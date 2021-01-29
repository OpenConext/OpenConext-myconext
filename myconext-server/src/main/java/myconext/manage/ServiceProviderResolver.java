package myconext.manage;

import myconext.model.ServiceProvider;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Optional;

public interface ServiceProviderResolver {

    void refresh();

    Optional<ServiceProvider> resolve(String entityId);
}
