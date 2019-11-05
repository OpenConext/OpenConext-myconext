package surfid.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml.provider.SamlServerConfiguration;
import org.springframework.security.saml.provider.config.SamlConfigurationRepository;
import org.springframework.security.saml.provider.identity.ResponseEnhancer;
import org.springframework.security.saml.provider.identity.config.SamlIdentityProviderServerBeanConfiguration;
import surfid.repository.AuthenticationRequestRepository;
import surfid.repository.UserRepository;
import surfid.saml.ImmutableSamlConfigurationRepository;
import surfid.security.GuestIdpAuthenticationRequestFilter;

import javax.servlet.Filter;

@Configuration
public class BeanConfig extends SamlIdentityProviderServerBeanConfiguration {

    private final String redirectUrl;
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;
    private final String spEntityId;

    public BeanConfig(@Value("${base_path}") String basePath,
                      @Value("${redirect_url}") String redirectUrl,
                      @Value("${sp_entity_id}") String spEntityId,
                      AuthenticationRequestRepository authenticationRequestRepository,
                      UserRepository userRepository) {
        this.immutableSamlConfigurationRepository = new ImmutableSamlConfigurationRepository(basePath);
        this.redirectUrl = redirectUrl;
        this.spEntityId = spEntityId;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
    }

    private ImmutableSamlConfigurationRepository immutableSamlConfigurationRepository;

    @Override
    protected SamlServerConfiguration getDefaultHostSamlServerConfiguration() {
        return new SamlServerConfiguration();
    }

    @Override
    public Filter idpAuthnRequestFilter() {
        return new GuestIdpAuthenticationRequestFilter(getSamlProvisioning(), samlAssertionStore(), redirectUrl,
                authenticationRequestRepository, userRepository, spEntityId);
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
