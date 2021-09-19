package myconext.manage;

import myconext.model.ServiceProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Profile("!test")
public class ManageServiceProviderResolver implements ServiceProviderResolver {

    private static final Log LOG = LogFactory.getLog(ManageServiceProviderResolver.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String manageBaseUrl;
    private Map<String, ServiceProvider> serviceProviders = new HashMap<>();
    private final HttpHeaders headers = new HttpHeaders();
    private final Map<String, Object> body = new HashMap<>();
    private final ParameterizedTypeReference<List<Map<String, Object>>> typeReference = new ParameterizedTypeReference<List<Map<String, Object>>>() {
    };

    public ManageServiceProviderResolver(@Value("${manage.username}") String userName,
                                         @Value("${manage.password}") String password,
                                         @Value("${manage.base_url}") String baseUrl) {
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(userName, password));
        this.manageBaseUrl = baseUrl;
        this.headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        this.body.put("state", "prodaccepted");
        this.body.put("REQUESTED_ATTRIBUTES", Arrays.asList(
                "metaDataFields.coin:application_url",
                "metaDataFields.logo:0:url",
                "metaDataFields.coin:institution_guid"
        ));
    }

    @Override
    @Scheduled(initialDelayString = "${cron.service-name-resolver-initial-delay-milliseconds}",
            fixedRateString = "${cron.service-name-resolver-fixed-rate-milliseconds}")
    public void refresh() {
        doRefresh(Optional.empty());
    }

    private void doRefresh(Optional<String> optionalEntityId) {
        long start = System.currentTimeMillis();
        try {
            Map<String, Object> requestBody = this.body;
            if (optionalEntityId.isPresent()) {
                requestBody = new HashMap<>(this.body);
                requestBody.put("entityid", optionalEntityId.get());
            }
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, this.headers);
            Map<String, ServiceProvider> newServiceProviders = exchangeAndParse(requestEntity, "saml20_sp");
            Map<String, ServiceProvider> relyingParties = exchangeAndParse(requestEntity, "oidc10_rp");
            newServiceProviders.putAll(relyingParties);
            if (optionalEntityId.isPresent()) {
                serviceProviders.putAll(newServiceProviders);
            } else {
                serviceProviders = newServiceProviders;
            }
            LOG.info("Refreshed all " + serviceProviders.size() + " Service providers and  Relying parties in " + (System.currentTimeMillis() - start) + " ms");
        } catch (Throwable t) {
            LOG.error("Error in refreshing metadata from " + manageBaseUrl, t);
        }
    }

    private Map<String, ServiceProvider> exchangeAndParse(HttpEntity<Map<String, Object>> requestEntity, String entityType) {
        return restTemplate.exchange(manageBaseUrl + "/manage/api/internal/search/" + entityType,
                HttpMethod.POST, requestEntity, typeReference) .getBody()
                .stream().collect(Collectors.toMap(this::entityId, this::serviceProvider));
    }

    @SuppressWarnings("unchecked")
    private String entityId(Map<String, Object> map) {
        Map<String, Object> data = (Map<String, Object>) map.get("data");
        return (String) data.get("entityid");
    }

    @SuppressWarnings("unchecked")
    private ServiceProvider serviceProvider(Map<String, Object> map) {
        Map<String, Object> data = (Map<String, Object>) map.get("data");
        Map<String, String> metaDataFields = (Map<String, String>) data.get("metaDataFields");
        return new ServiceProvider(
                metaDataFields.get("name:en"),
                metaDataFields.get("name:nl"),
                metaDataFields.get("logo:0:url"),
                metaDataFields.get("coin:application_url"),
                metaDataFields.get("coin:institution_guid")
        );
    }

    @Override
    public Optional<ServiceProvider> resolve(String entityId) {
        //For Testing purposes
        if (serviceProviders.isEmpty()) {
            refresh();
        }
        Optional<ServiceProvider> optionalServiceProvider = Optional.ofNullable(serviceProviders.get(entityId));
        if (!optionalServiceProvider.isPresent()) {
            //rare case, but it might be a entity thas was added after the last refresh
            doRefresh(Optional.of(entityId));
            return Optional.ofNullable(serviceProviders.get(entityId));
        }
        return optionalServiceProvider;
    }
}
