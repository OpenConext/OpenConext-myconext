package myconext.manage;

import myconext.model.ServiceProvider;

import java.util.Optional;

public class MockServiceProviderResolver implements ServiceProviderResolver{

    private ServiceProvider serviceProvider = new ServiceProvider(
            "Mine eduID",
            "Mijn eduID",
            "https://static.surfconext.nl/media/sp/eduid.png",
            "https://eduid.nl/");

    @Override
    public void refresh() {

    }

    @Override
    public Optional<ServiceProvider> resolve(String entityId) {
        return Optional.of(serviceProvider);
    }
}
