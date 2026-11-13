package myconext.invite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calls the Invite eduID API. The results are cached per organization for a limited time: staleness within a
 * session is acceptable and there are only a few different organizations. When Invite is not available the
 * last known (stale) result is used.
 */
public class RemoteInvite implements Invite {

    private static final Log LOG = LogFactory.getLog(RemoteInvite.class);

    private record CacheEntry(Map<String, List<String>> roles, long expiresAt) {
    }

    private final RestTemplate restTemplate = new RestTemplate();
    private final String guestsUrl;
    private final long ttlMillis;
    private final Clock clock;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public RemoteInvite(String userName, String password, String baseUrl, Duration ttl) {
        this(userName, password, baseUrl, ttl, Clock.systemUTC());
    }

    RemoteInvite(String userName, String password, String baseUrl, Duration ttl, Clock clock) {
        this.restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(userName, password));
        this.guestsUrl = baseUrl.replaceAll("/+$", "") + "/api/external/v1/eduid/guests/{organizationGUID}";
        this.ttlMillis = ttl.toMillis();
        this.clock = clock;
    }

    @Override
    public Map<String, List<String>> rolesByEduPersonPrincipalName(String organizationGUID) {
        // compute is atomic per key, so concurrent requests for one organization lead to one remote call
        return cache.compute(organizationGUID, (key, current) -> {
            long now = clock.millis();
            if (current != null && current.expiresAt() > now) {
                return current;
            }
            try {
                return new CacheEntry(fetch(key), now + ttlMillis);
            } catch (RuntimeException e) {
                LOG.warn("Failed to fetch roles from Invite for organization " + key
                        + (current != null ? ", using stale result" : ""), e);
                // Keep the stale result, but retry soon instead of on every request
                return current != null ? new CacheEntry(current.roles(), now + Math.min(ttlMillis, 30_000L)) :
                        new CacheEntry(Map.of(), now + Math.min(ttlMillis, 30_000L));
            }
        }).roles();
    }

    private Map<String, List<String>> fetch(String organizationGUID) {
        List<Map<String, Object>> guests = restTemplate.exchange(guestsUrl, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {
                }, organizationGUID).getBody();
        Map<String, List<String>> result = new HashMap<>();
        if (guests != null) {
            guests.forEach(guest -> {
                Object eppn = guest.get("eduperson_principal_name");
                if (eppn instanceof String name && guest.get("roles") instanceof List<?> roles) {
                    result.put(name, roles.stream().map(String::valueOf).toList());
                }
            });
        }
        return Map.copyOf(result);
    }

}
