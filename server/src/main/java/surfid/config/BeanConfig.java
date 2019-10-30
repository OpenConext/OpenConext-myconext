package surfid.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml.provider.SamlServerConfiguration;
import org.springframework.security.saml.provider.config.SamlConfigurationRepository;
import org.springframework.security.saml.provider.identity.ResponseEnhancer;
import org.springframework.security.saml.provider.identity.config.SamlIdentityProviderServerBeanConfiguration;
import surfid.repository.AuthenticationRequestRepository;
import surfid.saml.ImmutableSamlConfigurationRepository;
import surfid.security.GuestIdpAuthenticationRequestFilter;

import javax.servlet.Filter;

@Configuration
public class BeanConfig extends SamlIdentityProviderServerBeanConfiguration {

    private final String redirectUrl;
    private final AuthenticationRequestRepository authenticationRequestRepository;

    public BeanConfig(@Value("${base_path}") String basePath,
                      @Value("${redirect_url}") String redirectUrl,
                      AuthenticationRequestRepository authenticationRequestRepository) {
        this.immutableSamlConfigurationRepository = new ImmutableSamlConfigurationRepository(basePath);
        this.redirectUrl = redirectUrl;
        this.authenticationRequestRepository = authenticationRequestRepository;
    }

    private ImmutableSamlConfigurationRepository immutableSamlConfigurationRepository;

    @Override
    protected SamlServerConfiguration getDefaultHostSamlServerConfiguration() {
        return new SamlServerConfiguration();
    }

    @Override
    public Filter idpAuthnRequestFilter() {
        return new GuestIdpAuthenticationRequestFilter(getSamlProvisioning(), samlAssertionStore(), redirectUrl, authenticationRequestRepository);
    }

    @Override
    public ResponseEnhancer samlResponseEnhancer() {
        return response -> {
            return response;
        };
    }

    public Filter samlConfigurationFilter(SamlServerConfiguration serverConfig) {
        this.immutableSamlConfigurationRepository.setConfiguration(serverConfig);
        return new NoopFilter();
    }

    @Override
    public SamlConfigurationRepository samlConfigurationRepository() {
        return immutableSamlConfigurationRepository;
    }

}
