package myconext.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ServiceNameResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceNameResolver.class);

    private final Resource metaDataResource;
    private final ObjectMapper objectMapper;
    private Map<String, Map<String, String>> serviceNames = new HashMap<>();

    @Autowired
    public ServiceNameResolver(@Value("${metadata_sp_url}") Resource metaDataResource, ObjectMapper objectMapper) {
        this.metaDataResource = metaDataResource;
        this.objectMapper = objectMapper;
        this.refresh();
    }

    @Scheduled(cron = "0 0/60 * * * *")
    public void refresh() {
        long start = System.currentTimeMillis();
        try {
            serviceNames = objectMapper.readValue(metaDataResource.getInputStream(), new
                    TypeReference<List<Map<String, Map<String, String>>>>() {
                    }).stream().collect(Collectors.toMap(m -> m.keySet().iterator().next(), m -> m.values().iterator().next()));
            LOG.info("Refreshed all " + serviceNames.size() + " Service names from " + metaDataResource + "in " + (System.currentTimeMillis() - start) + " ms");
        } catch (Throwable t) {
            LOG.error("Error in refreshing service names from " + metaDataResource, t);
        }
    }

    public String resolve(String entityId) {
        Map<String, String> names = serviceNames.getOrDefault(entityId, Collections.emptyMap());
        String name = names.getOrDefault("en", names.get("nl"));
        return StringUtils.hasText(name) ? name : entityId;
    }
}
