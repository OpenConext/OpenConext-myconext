package myconext.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.model.ServiceProvider;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ManageConfigurationTest {

    @Test
    void manage() {
        ManageConfiguration configuration = new ManageConfiguration();
        Manage serviceProviderResolver = configuration.manage(null, null, null, false, new ObjectMapper());
        ServiceProvider serviceProvider = serviceProviderResolver.findServiceProviderByEntityId("mock-sp").get();
        assertEquals("OpenConext Mujina SP", serviceProvider.getName());

        Optional<ServiceProvider> optionalServiceProvider = serviceProviderResolver.findServiceProviderByEntityId("nope");
        assertFalse(optionalServiceProvider.isPresent());

    }
}