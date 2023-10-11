package myconext.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.model.ServiceProvider;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockEnvironment;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ManageServiceProviderConfigurationTest {

    @Test
    void serviceProviderResolver() {
        ManageServiceProviderConfiguration configuration = new ManageServiceProviderConfiguration();
        ServiceProviderResolver serviceProviderResolver = configuration.serviceProviderResolver(null, null, null, false,
                new ClassPathResource("metadata/sp-names.json"),
                new ObjectMapper(),
                new MockEnvironment());
        ServiceProvider serviceProvider = serviceProviderResolver.resolve("http://mock-sp").get();
        assertEquals("SURFconext Mujina SP", serviceProvider.getName());

        Optional<ServiceProvider> optionalServiceProvider = serviceProviderResolver.resolve("nope");
        assertFalse(optionalServiceProvider.isPresent());

    }
}