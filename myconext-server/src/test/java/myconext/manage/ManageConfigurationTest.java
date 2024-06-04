package myconext.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.model.IdentityProvider;
import myconext.model.ServiceProvider;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ManageConfigurationTest {

    @Test
    void manage() {
        ManageConfiguration configuration = new ManageConfiguration();
        Manage manage = configuration.manage(null, null, null, false, new ObjectMapper());
        ServiceProvider serviceProvider = manage.findServiceProviderByEntityId("mock-sp").get();
        assertEquals("OpenConext Mujina SP", serviceProvider.getName());

        Optional<ServiceProvider> optionalServiceProvider = manage.findServiceProviderByEntityId("nope");
        assertFalse(optionalServiceProvider.isPresent());

        IdentityProvider identityProvider = manage.findIdentityProviderByBrinCode("ST42").get();
        assertEquals("https://static.surfconext.nl/media/idp/avat_st.jpg", identityProvider.getLogoUrl());

    }
}