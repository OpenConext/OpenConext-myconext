package myconext.config;

import myconext.geo.GeoLocation;
import myconext.mail.MailBox;
import myconext.manage.ServiceProviderResolver;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.UserLoginRepository;
import myconext.repository.UserRepository;
import myconext.saml.ImmutableSamlConfigurationRepository;
import myconext.security.ACR;
import myconext.security.GuestIdpAuthenticationRequestFilter;
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
    private final UserLoginRepository userLoginRepository;
    private final int rememberMeMaxAge;
    private final int nudgeAppDays;
    private final int rememberMeQuestionAskedDays;
    private final long expiryNonValidatedDurationDays;
    private final long removalNonValidatedDurationDays;
    private final boolean secureCookie;
    private final String magicLinkUrl;
    private final MailBox mailBox;
    private final ServiceProviderResolver serviceProviderResolver;
    private final GeoLocation geoLocation;

    public BeanConfig(@Value("${saml_metadata_base_path}") String samlMetadataBasePath,
                      @Value("${idp_redirect_url}") String redirectUrl,
                      @Value("${remember_me_max_age_seconds}") int rememberMeMaxAge,
                      @Value("${nudge_eduid_app_days}") int nudgeAppDays,
                      @Value("${remember_me_question_asked_days}") int rememberMeQuestionAskedDays,
                      @Value("${secure_cookie}") boolean secureCookie,
                      @Value("${email.magic-link-url}") String magicLinkUrl,
                      @Value("${account_linking_context_class_ref.linked_institution}") String linkedInstitution,
                      @Value("${account_linking_context_class_ref.validate_names}") String validateNames,
                      @Value("${account_linking_context_class_ref.affiliation_student}") String affiliationStudent,
                      @Value("${account_linking_context_class_ref.profile_mfa}") String profileMfa,
                      @Value("${linked_accounts.expiry-duration-days-non-validated}") long expiryNonValidatedDurationDays,
                      @Value("${linked_accounts.removal-duration-days-non-validated}") long removalNonValidatedDurationDays,
                      AuthenticationRequestRepository authenticationRequestRepository,
                      UserRepository userRepository,
                      UserLoginRepository userLoginRepository,
                      GeoLocation geoLocation,
                      MailBox mailBox,
                      ServiceProviderResolver serviceProviderResolver) {
        this.immutableSamlConfigurationRepository = new ImmutableSamlConfigurationRepository(samlMetadataBasePath);
        this.redirectUrl = redirectUrl;
        this.rememberMeMaxAge = rememberMeMaxAge;
        this.nudgeAppDays = nudgeAppDays;
        this.rememberMeQuestionAskedDays = rememberMeQuestionAskedDays;
        this.secureCookie = secureCookie;
        this.expiryNonValidatedDurationDays = expiryNonValidatedDurationDays;
        this.removalNonValidatedDurationDays = removalNonValidatedDurationDays;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.userLoginRepository = userLoginRepository;
        this.geoLocation = geoLocation;
        this.magicLinkUrl = magicLinkUrl;
        this.mailBox = mailBox;
        this.serviceProviderResolver = serviceProviderResolver;

        ACR.initialize(linkedInstitution, validateNames, affiliationStudent, profileMfa);
    }

    private final ImmutableSamlConfigurationRepository immutableSamlConfigurationRepository;

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
                serviceProviderResolver,
                authenticationRequestRepository,
                userRepository,
                userLoginRepository,
                geoLocation,
                rememberMeMaxAge,
                nudgeAppDays,
                rememberMeQuestionAskedDays,
                secureCookie,
                magicLinkUrl,
                mailBox,
                expiryNonValidatedDurationDays,
                removalNonValidatedDurationDays);
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
