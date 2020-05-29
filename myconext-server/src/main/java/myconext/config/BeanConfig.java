package myconext.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.mail.MailBox;
import myconext.manage.ServiceNameResolver;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.UserRepository;
import myconext.saml.ImmutableSamlConfigurationRepository;
import myconext.security.GuestIdpAuthenticationRequestFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml.provider.SamlServerConfiguration;
import org.springframework.security.saml.provider.config.SamlConfigurationRepository;
import org.springframework.security.saml.provider.identity.config.SamlIdentityProviderServerBeanConfiguration;

import javax.servlet.Filter;

@Configuration
public class BeanConfig extends SamlIdentityProviderServerBeanConfiguration {

    private final String redirectUrl;
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;
    private final String accountLinkingAuthenticationContextClassReferenceValue;
    private final int rememberMeMaxAge;
    private final boolean secureCookie;
    private final String magicLinkUrl;
    private final MailBox mailBox;
    private final ServiceNameResolver serviceNameResolver;
    private final ObjectMapper objectMapper;

    public BeanConfig(@Value("${base_path}") String basePath,
                      @Value("${idp_redirect_url}") String redirectUrl,
                      @Value("${remember_me_max_age_seconds}") int rememberMeMaxAge,
                      @Value("${secure_cookie}") boolean secureCookie,
                      @Value("${email.magic-link-url}") String magicLinkUrl,
                      @Value("${account_linking_authentication_context_class_reference_value}") String accountLinkingAuthenticationContextClassReferenceValue,
                      AuthenticationRequestRepository authenticationRequestRepository,
                      UserRepository userRepository,
                      MailBox mailBox,
                      ServiceNameResolver serviceNameResolver,
                      @Qualifier("jsonMapper") ObjectMapper objectMapper) {
        this.immutableSamlConfigurationRepository = new ImmutableSamlConfigurationRepository(basePath);
        this.redirectUrl = redirectUrl;
        this.rememberMeMaxAge = rememberMeMaxAge;
        this.secureCookie = secureCookie;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.magicLinkUrl = magicLinkUrl;
        this.accountLinkingAuthenticationContextClassReferenceValue = accountLinkingAuthenticationContextClassReferenceValue;
        this.mailBox = mailBox;
        this.serviceNameResolver = serviceNameResolver;
        this.objectMapper = objectMapper;
    }

    private ImmutableSamlConfigurationRepository immutableSamlConfigurationRepository;

    @Override
    protected SamlServerConfiguration getDefaultHostSamlServerConfiguration() {
        return new SamlServerConfiguration();
    }

    @Override
    public Filter idpAuthnRequestFilter() {
        return new GuestIdpAuthenticationRequestFilter(
                getSamlProvisioning(),
                samlAssertionStore(),
                redirectUrl,
                serviceNameResolver,
                authenticationRequestRepository,
                userRepository,
                accountLinkingAuthenticationContextClassReferenceValue,
                rememberMeMaxAge,
                secureCookie,
                magicLinkUrl,
                mailBox);
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
