package myconext.manage;

import myconext.model.ServiceProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MockServiceProviderResolver implements ServiceProviderResolver {

    private final Map<String, ServiceProvider> serviceProviders = new HashMap<>();

    public MockServiceProviderResolver() {
        serviceProviders.put("http://mock-sp", new ServiceProvider(
                "Mock SP",
                "Mock SP NL",
                "https://static.surfconext.nl/media/sp/eduid.png",
                "https://mock.sp/",
                "ad93daef-0911-e511-80d0-005056956c1a"));
        serviceProviders.put("playground_client", new ServiceProvider(
                "Playground Client",
                "Playground Client",
                "https://static.surfconext.nl/media/sp/eduid.png",
                "https://playground.client/",
                "ad93daef-0911-e511-80d0-005056956c1a"));
        serviceProviders.put("noInstitutionalGuid", new ServiceProvider(
                "Nice SP",
                "Nice SP NL",
                null, null, null));
    }

    @Override
    public void refresh() {
    }

    @Override
    public Optional<ServiceProvider> resolve(String entityId) {
        return Optional.ofNullable(serviceProviders.get(entityId));
    }
}
