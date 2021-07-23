package myconext.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.model.ServiceProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MetaDataServiceProviderResolver implements ServiceProviderResolver {

    private static final Log LOG = LogFactory.getLog(MetaDataServiceProviderResolver.class);

    private final Resource metaDataResource;
    private final ObjectMapper objectMapper;
    private Map<String, ServiceProvider> serviceProviders = new HashMap<>();

    @Autowired
    public MetaDataServiceProviderResolver(@Value("${metadata_sp_url}") Resource metaDataResource,
                                           @Qualifier("jsonMapper") ObjectMapper objectMapper) {
        this(metaDataResource, objectMapper, true);
    }

    public MetaDataServiceProviderResolver(@Value("${metadata_sp_url}") Resource metaDataResource,
                                           ObjectMapper objectMapper,
                                           boolean lazy) {
        this.metaDataResource = metaDataResource;
        this.objectMapper = objectMapper;
        if (!lazy) {
            this.refresh();
        }
    }

    @Override
    @Scheduled(initialDelayString = "${cron.service-name-resolver-initial-delay-milliseconds}",
            fixedRateString = "${cron.service-name-resolver-fixed-rate-milliseconds}")
    public void refresh() {
        long start = System.currentTimeMillis();
        try {
            serviceProviders = objectMapper.readValue(metaDataResource.getInputStream(), new
                    TypeReference<List<Map<String, Map<String, String>>>>() {
                    }).stream()
                    .collect(Collectors.toMap(m -> m.keySet().iterator().next(), m -> this.serviceProvider(m)));
            LOG.info("Refreshed all " + serviceProviders.size() + " Service providers " + metaDataResource + " in " + (System.currentTimeMillis() - start) + " ms");
        } catch (Throwable t) {
            LOG.error("Error in refreshing service names from " + metaDataResource, t);
        }
    }

    private ServiceProvider serviceProvider(Map<String, Map<String, String>> map) {
        /*
         *  {
         * "http://mock-sp": {
         *   "en": "SURFconext Mujina SP",
         *   "nl": "SURFconext Mujina SP NL",
         *   "home_url": "https://mujina-sp.test2.surfconext.nl/",
         *   "logo_url": "https://static.surfconext.nl/logos/sp/surfmailfilter.png"
         *  }
         * }
         */
        Map<String, String> values = map.values().iterator().next();
        return new ServiceProvider(values.get("en"), values.get("nl"),values.get("logo_url"),values.get("home_url"));
    }

    @Override
    public Optional<ServiceProvider> resolve(String entityId) {
        return Optional.ofNullable(serviceProviders.get(entityId));
    }
}
