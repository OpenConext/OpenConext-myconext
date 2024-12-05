package myconext.manage;

import myconext.model.IdentityProvider;
import myconext.model.RemoteProvider;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.ServiceProvider;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toMap;
import static myconext.security.CookieResolver.cookieByName;

@SuppressWarnings("unchecked")
public interface Manage {

    Optional<ServiceProvider> findServiceProviderByEntityId(String entityId);

    Set<String> getDomainNames();

    Optional<IdentityProvider> findIdentityProviderByDomainName(String domainName);

    Optional<IdentityProvider> findIdentityProviderByBrinCode(String brinCode);

    Optional<IdentityProvider> findIdentityProviderByInstitutionGUID(String institutionGUID);

    default String getServiceName(HttpServletRequest request, SamlAuthenticationRequest samlAuthenticationRequest) {
        String lang = cookieByName(request, "lang").map(Cookie::getValue).orElse("en");
        String requesterEntityId = samlAuthenticationRequest.getRequesterEntityId();
        Optional<ServiceProvider> optionalServiceProvider = findServiceProviderByEntityId(requesterEntityId);
        return optionalServiceProvider
                .map(serviceProvider -> lang.equals("en") ? serviceProvider.getName() : serviceProvider.getNameNl())
                .orElse(requesterEntityId);
    }

    default Map<String, IdentityProvider> mergeByDomainNames(List<Map<String, Object>> manageIdentityProviders) {
        return manageIdentityProviders.stream()
                .reduce(new HashMap<>(),
                        (acc, map) -> {
                            Map<String, IdentityProvider> providerMap = identityProvider(map);
                            acc.putAll(providerMap);
                            return acc;
                        }
                ).entrySet().stream().collect(toMap(e -> e.getKey(), e -> (IdentityProvider) e.getValue()));
    }

    default String entityId(Map<String, Object> map) {
        Map<String, Object> data = (Map<String, Object>) map.get("data");
        return (String) data.get("entityid");
    }

    default ServiceProvider serviceProvider(Map<String, Object> map) {
        RemoteProvider remoteProvider = remoteProvider(map);
        return new ServiceProvider(remoteProvider,
                metaDataFields(map).get("coin:application_url"));
    }

    @SuppressWarnings("unchecked")
    default Map<String, String> metaDataFields(Map<String, Object> map) {
        Map<String, Object> data = (Map<String, Object>) map.get("data");
        return (Map<String, String>) data.get("metaDataFields");
    }

    @SuppressWarnings("unchecked")
    default RemoteProvider remoteProvider(Map<String, Object> map) {
        Map<String, Object> data = (Map<String, Object>) map.get("data");
        String entityId = (String) data.get("entityid");

        Map<String, String> metaDataFields = (Map<String, String>) data.get("metaDataFields");
        String nameEn = metaDataFields.get("name:en");
        String nameNl = metaDataFields.get("name:nl");
        return new RemoteProvider(
                entityId,
                StringUtils.hasText(nameEn) ? nameEn : StringUtils.hasText(nameNl) ? nameNl : entityId,
                StringUtils.hasText(nameNl) ? nameNl : StringUtils.hasText(nameEn) ? nameEn : entityId,
                metaDataFields.get("coin:institution_guid"),
                metaDataFields.get("logo:0:url")
        );

    }

    @SuppressWarnings("unchecked")
    default Map<String, IdentityProvider> identityProvider(Map<String, Object> map) {
        RemoteProvider remoteProvider = remoteProvider(map);
        Map<String, String> metaDataFields = metaDataFields(map);
        IdentityProvider identityProvider = new IdentityProvider(
                remoteProvider,
                metaDataFields.get("coin:institution_brin"),
                metaDataFields.get("shibmd:scope:0:allowed")
        );
        Map<String, IdentityProvider> results = new HashMap<>();

        IntStream.range(0, 10).forEach(i -> {
            String scope = metaDataFields.get(String.format("shibmd:scope:%s:allowed", i));
            if (StringUtils.hasText(scope)) {
                results.put(scope, identityProvider);
            }
        });
        return results;
    }
}
