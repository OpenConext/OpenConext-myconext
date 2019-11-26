package myconext.security;

import myconext.config.BeanConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.saml.provider.SamlServerConfiguration;
import org.springframework.security.saml.provider.identity.config.SamlIdentityProviderSecurityDsl;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;

import javax.servlet.Filter;

public class GuestIdentityProviderDsl extends SamlIdentityProviderSecurityDsl {

    private final BeanConfig beanConfig;

    GuestIdentityProviderDsl(BeanConfig beanConfig) {
        this.beanConfig = beanConfig;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {

        super.configure(http);

        ApplicationContext context = http.getSharedObject(ApplicationContext.class);

        SamlServerConfiguration serverConfig = context.getBean("idpSamlServerConfiguration", SamlServerConfiguration.class);

        Filter samlConfigurationFilter = beanConfig.samlConfigurationFilter(serverConfig);
        Filter metadataFilter = beanConfig.idpMetadataFilter();
        Filter idpAuthnRequestFilter = beanConfig.idpAuthnRequestFilter();
        http
                .addFilterAfter(
                        samlConfigurationFilter,
                        SecurityContextPersistenceFilter.class
                )
                .addFilterAfter(
                        metadataFilter,
                        samlConfigurationFilter.getClass()
                )
                .addFilterAfter(
                        idpAuthnRequestFilter,
                        metadataFilter.getClass()
                );
    }
}
