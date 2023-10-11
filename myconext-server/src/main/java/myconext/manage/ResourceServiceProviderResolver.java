package myconext.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import myconext.model.ServiceProvider;
import org.apache.commons.io.IOUtil;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ResourceServiceProviderResolver implements ServiceProviderResolver {

    private final List<Map<String, Map<String, String>>> spNames;

    @SneakyThrows
    public ResourceServiceProviderResolver(Resource resource, ObjectMapper objectMapper) {
        spNames = objectMapper.readValue(IOUtil.toString(resource.getInputStream()), new TypeReference<>() {
        });
    }

    @Override
    public void refresh() {
        //noop
    }

    @Override
    public Optional<ServiceProvider> resolve(String entityId) {
        return spNames.stream().filter(m -> m.containsKey(entityId))
                .findFirst()
                .map(entry -> {
                    Map<String, String> data = entry.get(entityId);
                    return new ServiceProvider(
                            data.get("en"),
                            data.get("nl"),
                            data.get("logo_url"),
                            data.get("home_url"),
                            data.get("institution_guid"));
                });

    }
}
