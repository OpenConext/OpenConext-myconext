package myconext.manage;

import myconext.model.ServiceProvider;

import java.util.Optional;

public interface ServiceProviderResolver {

    void refresh();

    Optional<ServiceProvider> resolve(String entityId);
}
