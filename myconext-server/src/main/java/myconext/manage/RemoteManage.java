package myconext.manage;

import myconext.model.IdentityProvider;
import myconext.model.ServiceProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

public class RemoteManage implements Manage {

    private static final Log LOG = LogFactory.getLog(RemoteManage.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final String manageBaseUrl;
    private Map<String, ServiceProvider> serviceProviders = new HashMap<>();
    private Map<String, IdentityProvider> identityProviders = new HashMap<>();
    private final HttpHeaders headers = new HttpHeaders();
    private final Map<String, Object> spRequestAttributes = new HashMap<>();
    private final Map<String, Object> idpRequestAttributes = new HashMap<>();
    private final ParameterizedTypeReference<List<Map<String, Object>>> typeReference = new ParameterizedTypeReference<>() {
    };

    public RemoteManage(String userName,
                        String password,
                        String baseUrl) {
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(userName, password));
        this.manageBaseUrl = baseUrl;
        this.headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        this.spRequestAttributes.put("REQUESTED_ATTRIBUTES", Arrays.asList(
                "metaDataFields.coin:application_url",
                "metaDataFields.logo:0:url",
                "metaDataFields.coin:institution_guid"
        ));
        this.idpRequestAttributes.put("REQUESTED_ATTRIBUTES", Arrays.asList(
                "metaDataFields.coin:institution_brin",
                "metaDataFields.logo:0:url",
                "metaDataFields.coin:institution_guid",
                "metaDataFields.shibmd:scope:0:allowed",
                "metaDataFields.shibmd:scope:1:allowed",
                "metaDataFields.shibmd:scope:2:allowed",
                "metaDataFields.shibmd:scope:3:allowed",
                "metaDataFields.shibmd:scope:4:allowed",
                "metaDataFields.shibmd:scope:5:allowed",
                "metaDataFields.shibmd:scope:6:allowed",
                "metaDataFields.shibmd:scope:7:allowed",
                "metaDataFields.shibmd:scope:8:allowed",
                "metaDataFields.shibmd:scope:9:allowed"
        ));
    }

    @Scheduled(initialDelayString = "${cron.manage-initial-delay-milliseconds}",
            fixedRateString = "${cron.manage-fixed-rate-milliseconds}")
    public void refresh() {
        long start = System.currentTimeMillis();
        LOG.info("Starting to refresh metadata from " + manageBaseUrl);
        doRefreshServiceProviders(Optional.empty());
        doRefreshIdentityProviders();
        LOG.info(String.format("Refreshed %s services and %s scoped  IdP's in %s ms",
                this.serviceProviders.size(),
                this.identityProviders.size(),
                System.currentTimeMillis() - start));

    }

    private void doRefreshIdentityProviders() {
        try {
            Map<String, Object> requestBody = new HashMap<>(idpRequestAttributes);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, this.headers);
            List<Map<String, Object>> manageIdentityProviders = restTemplate.exchange(manageBaseUrl + "/manage/api/internal/search/saml20_idp",
                    HttpMethod.POST, requestEntity, typeReference).getBody();
            identityProviders = mergeByDomainNames(manageIdentityProviders);
        } catch (Throwable t) {
            LOG.error("Error in refreshing metadata from " + manageBaseUrl, t);
        }
    }

    private void doRefreshServiceProviders(Optional<String> optionalEntityId) {
        try {
            //Need to copy because of re-use
            Map<String, Object> requestBody = new HashMap<>(spRequestAttributes);
            optionalEntityId.ifPresent(s -> requestBody.put("entityid", s));

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, this.headers);
            Map<String, ServiceProvider> newServiceProviders = exchangeAndParse(requestEntity, "saml20_sp");
            Map<String, ServiceProvider> relyingParties = exchangeAndParse(requestEntity, "oidc10_rp");
            newServiceProviders.putAll(relyingParties);

            if (optionalEntityId.isPresent()) {
                serviceProviders.putAll(newServiceProviders);
            } else {
                serviceProviders = newServiceProviders;
            }
        } catch (Throwable t) {
            LOG.error("Error in refreshing metadata from " + manageBaseUrl, t);
        }
    }

    private Map<String, ServiceProvider> exchangeAndParse(HttpEntity<Map<String, Object>> requestEntity, String entityType) {
        return restTemplate.exchange(manageBaseUrl + "/manage/api/internal/search/" + entityType,
                        HttpMethod.POST, requestEntity, typeReference).getBody()
                .stream().collect(Collectors.toMap(this::entityId, this::serviceProvider));
    }

    @Override
    public Optional<ServiceProvider> findServiceProviderByEntityId(String entityId) {
        //For Testing purposes
        if (serviceProviders.isEmpty()) {
            LOG.info("Refreshing metadata as the current collection is empty");
            doRefreshServiceProviders(Optional.empty());
        }
        Optional<ServiceProvider> optionalServiceProvider = Optional.ofNullable(serviceProviders.get(entityId));
        if (optionalServiceProvider.isEmpty()) {
            LOG.info("Refreshing metadata because entityID " + entityId + " not in present collection");
            //very rare case, but it might be an entity that was added after the last refresh
            doRefreshServiceProviders(Optional.of(entityId));
            return Optional.ofNullable(serviceProviders.get(entityId));
        }
        return optionalServiceProvider;
    }

    @Override
    public Set<String> getDomainNames() {
        //For Testing purposes
        if (identityProviders.isEmpty()) {
            LOG.info("Refreshing metadata as the current collection is empty");
            doRefreshIdentityProviders();
        }
        return identityProviders.keySet();
    }

    @Override
    public Optional<IdentityProvider> findIdentityProviderByDomainName(String domainName) {
        //For Testing purposes
        if (identityProviders.isEmpty()) {
            LOG.info("Refreshing metadata as the current collection is empty");
            doRefreshIdentityProviders();
        }
        return Optional.ofNullable(this.identityProviders.get(domainName));
    }

    @Override
    public Optional<IdentityProvider> findIdentityProviderByBrinCode(String brinCode) {
        return searchIdentityProvider("metaDataFields.coin:institution_brin", brinCode);
    }

    @Override
    public Optional<IdentityProvider> findIdentityProviderByInstitutionGUID(String institutionGUID) {
        return searchIdentityProvider("metaDataFields.coin:institution_guid", institutionGUID);
    }

    private Optional<IdentityProvider> searchIdentityProvider(String metaDataField, String metaDataValue) {
        Map<String, Object> requestBody = Map.of(metaDataField, metaDataValue,
                "REQUESTED_ATTRIBUTES", Arrays.asList(
                        "metaDataFields.coin:institution_brin",
                        "metaDataFields.logo:0:url",
                        "metaDataFields.coin:institution_guid",
                        "metaDataFields.shibmd:scope:0:allowed"));
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, this.headers);
        return restTemplate.exchange(manageBaseUrl + "/manage/api/internal/search/saml20_idp",
                        HttpMethod.POST, requestEntity, typeReference)
                .getBody()
                .stream()
                .map(m -> new IdentityProvider(
                        remoteProvider(m),
                        metaDataFields(m).get("coin:institution_brin"),
                        metaDataFields(m).get("shibmd:scope:0:allowed")))
                .findFirst();
    }

}

