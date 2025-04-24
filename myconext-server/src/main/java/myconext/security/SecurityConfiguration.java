package myconext.security;

import lombok.SneakyThrows;
import myconext.crypto.KeyGenerator;
import myconext.geo.GeoLocation;
import myconext.mail.MailBox;
import myconext.manage.Manage;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.UserLoginRepository;
import myconext.repository.UserRepository;
import myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import myconext.shibboleth.ShibbolethUserDetailService;
import myconext.shibboleth.mock.MockShibbolethFilter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.Resource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import saml.model.SAMLConfiguration;
import saml.model.SAMLIdentityProvider;
import saml.model.SAMLServiceProvider;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Configuration
//TODO replace WebSecurityConfigurerAdapter with SecurityFilterChain
@SuppressWarnings("deprecation")
public class SecurityConfiguration {

    private static final Log LOG = LogFactory.getLog(SecurityConfiguration.class);

    @Configuration
    @Order(1)
    @EnableConfigurationProperties(IdentityProviderMetaData.class)
    public static class SamlSecurity extends WebSecurityConfigurerAdapter {

        private final GuestIdpAuthenticationRequestFilter guestIdpAuthenticationRequestFilter;

        public SamlSecurity(@Value("${private_key_path}") Resource privateKeyPath,
                            @Value("${certificate_path}") Resource certificatePath,
                            @Value("${idp_entity_id}") String idpEntityId,
                            @Value("${sp_entity_id}") String spEntityId,
                            @Value("${sp_entity_metadata_url}") String spMetaDataUrl,
                            @Value("${saml_metadata_base_path}") String samlMetadataBasePath,
                            @Value("${idp_redirect_url}") String redirectUrl,
                            @Value("${remember_me_max_age_seconds}") int rememberMeMaxAge,
                            @Value("${nudge_eduid_app_login_days}") int nudgeAppDays,
                            @Value("${nudge_eduid_app_pause_days}") int nudgeAppDelayDays,
                            @Value("${remember_me_question_asked_days}") int rememberMeQuestionAskedDays,
                            @Value("${secure_cookie}") boolean secureCookie,
                            @Value("${email.magic-link-url}") String magicLinkUrl,
                            @Value("${account_linking_context_class_ref.linked_institution}") String linkedInstitution,
                            @Value("${account_linking_context_class_ref.validate_names}") String validateNames,
                            @Value("${account_linking_context_class_ref.validate_names_external}") String validateNamesExternal,
                            @Value("${account_linking_context_class_ref.affiliation_student}") String affiliationStudent,
                            @Value("${account_linking_context_class_ref.profile_mfa}") String profileMfa,
                            @Value("${linked_accounts.expiry-duration-days-non-validated}") long expiryNonValidatedDurationDays,
                            @Value("${sso_mfa_duration_seconds}") long ssoMFADurationSeconds,
                            @Value("${mobile_app_rp_entity_id}") String mobileAppROEntityId,
                            @Value("${feature.default_remember_me}") boolean featureDefaultRememberMe,
                            @Value("${feature.requires_signed_authn_request}") boolean requiresSignedAuthnRequest,
                            AuthenticationRequestRepository authenticationRequestRepository,
                            UserRepository userRepository,
                            UserLoginRepository userLoginRepository,
                            GeoLocation geoLocation,
                            MailBox mailBox,
                            Manage serviceProviderResolver,
                            IdentityProviderMetaData identityProviderMetaData,
                            CookieValueEncoder cookieValueEncoder) {
            ACR.initialize(
                    linkedInstitution,
                    validateNames,
                    validateNamesExternal,
                    affiliationStudent
            );
            String[] keys = this.getKeys(certificatePath, privateKeyPath);
            final List<SAMLServiceProvider> serviceProviders = new ArrayList<>();

            List<String> spEntityIdentifiers = commaSeparatedToList(spEntityId);
            List<String> spMetaDataUrls = commaSeparatedToList(spMetaDataUrl);
            for (int i = 0; i < spEntityIdentifiers.size(); i++) {
                serviceProviders.add(new SAMLServiceProvider(spEntityIdentifiers.get(i), spMetaDataUrls.get(i)));
            }

            SAMLConfiguration configuration = new SAMLConfiguration(
                    new SAMLIdentityProvider(keys[0], keys[1], idpEntityId),
                    serviceProviders,
                    requiresSignedAuthnRequest
            );
            this.guestIdpAuthenticationRequestFilter = new GuestIdpAuthenticationRequestFilter(
                    redirectUrl,
                    serviceProviderResolver,
                    authenticationRequestRepository,
                    userRepository,
                    userLoginRepository,
                    geoLocation,
                    rememberMeMaxAge,
                    nudgeAppDays,
                    nudgeAppDelayDays,
                    rememberMeQuestionAskedDays,
                    secureCookie,
                    magicLinkUrl,
                    mailBox,
                    expiryNonValidatedDurationDays,
                    ssoMFADurationSeconds,
                    mobileAppROEntityId,
                    featureDefaultRememberMe,
                    configuration,
                    identityProviderMetaData,
                    cookieValueEncoder
            );

        }

        private List<String> commaSeparatedToList(String spEntityId) {
            return Arrays.stream(spEntityId.split(",")).map(String::trim).collect(toList());
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatchers()
                    .antMatchers("/saml/guest-idp/**")
                    .and()
                    .csrf()
                    .disable()
                    .addFilterBefore(this.guestIdpAuthenticationRequestFilter,
                            AbstractPreAuthenticatedProcessingFilter.class
                    )
                    .authorizeRequests()
                    .antMatchers("/**").hasRole("GUEST");
        }

        @SneakyThrows
        private String[] getKeys(Resource certificatePath, Resource privateKeyPath) {
            String privateKey;
            String certificate;
            if (privateKeyPath.exists() && certificatePath.exists()) {
                privateKey = read(privateKeyPath);
                certificate = read(certificatePath);
            } else {
                String[] keys = KeyGenerator.generateKeys();
                privateKey = keys[0];
                certificate = keys[1];
            }
            return new String[]{certificate, privateKey};
        }

        private String read(Resource resource) throws IOException {
            LOG.info("Reading resource: " + resource.getFilename());
            return IOUtils.toString(resource.getInputStream(), Charset.defaultCharset());
        }
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        //because Autowired this will end up in the global ProviderManager
        PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(new ShibbolethUserDetailService());
        auth.authenticationProvider(authenticationProvider);
    }

    @Order
    @Configuration
    public static class InternalSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        private final Environment environment;
        private final UserRepository userRepository;
        private final Manage serviceProviderResolver;
        private final String mijnEduIDEntityId;

        public InternalSecurityConfigurationAdapter(Environment environment,
                                                    UserRepository userRepository,
                                                    Manage serviceProviderResolver,
                                                    @Value("${mijn_eduid_entity_id}") String mijnEduIDEntityId) {
            this.environment = environment;
            this.userRepository = userRepository;
            this.serviceProviderResolver = serviceProviderResolver;
            this.mijnEduIDEntityId = mijnEduIDEntityId;
        }

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers(
                    "/internal/**",
                    "/myconext/api/idp/**",
                    "/myconext/api/sp/create-from-institution",
                    "/myconext/api/sp/create-from-institution/**",
                    "/myconext/api/sp/idin/issuers"
            );
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatchers()
                    .antMatchers("/myconext/api/sp/**", "/startSSO", "/tiqr/sp/**")
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                    .csrf()
                    .disable()
                    .addFilterBefore(
                            new ShibbolethPreAuthenticatedProcessingFilter(
                                    authenticationManagerBean(),
                                    userRepository,
                                    serviceProviderResolver,
                                    mijnEduIDEntityId),
                            AbstractPreAuthenticatedProcessingFilter.class
                    )
                    .authorizeRequests()
                    .antMatchers("/**").hasRole("GUEST");

            if (environment.acceptsProfiles(Profiles.of("test", "test2", "dev"))) {
                //we can't use @Profile, because we need to add it before the real filter
                http.addFilterBefore(new MockShibbolethFilter(), ShibbolethPreAuthenticatedProcessingFilter.class);
            }
        }
    }

    @Configuration
    @Order(2)
    @EnableConfigurationProperties({ExternalApiConfiguration.class})
    public static class AppSecurity extends WebSecurityConfigurerAdapter {

        private final ExternalApiConfiguration remoteUsers;

        public AppSecurity(ExternalApiConfiguration remoteUsers) {
            this.remoteUsers = remoteUsers;
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            String[] antPatterns = {
                    "/myconext/api/attribute-aggregation/**",
                    "/myconext/api/attribute-manipulation/**",
                    "/myconext/api/system/**",
                    "/myconext/api/invite/**",
                    "/api/remote-creation/**"
            };
            http.requestMatchers()
                    .antMatchers(antPatterns)
                    .and()
                    .csrf()
                    .disable()
                    .authorizeRequests()
                    .antMatchers(antPatterns)
                    .authenticated()
                    .and()
                    .httpBasic()
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.userDetailsService(userDetailsService());
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return new ExtendedInMemoryUserDetailsManager(remoteUsers.getRemoteUsers());
        }
    }

    @Configuration
    @Order(3)
    public static class JWTSecurityConfig extends WebSecurityConfigurerAdapter {

        @Value("${eduid_api.oidcng_introspection_uri}")
        private String introspectionUri;

        @Value("${eduid_api.oidcng_client_id}")
        private String clientId;

        @Value("${eduid_api.oidcng_secret}")
        private String secret;

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers(
                    "/mobile/api/idp/create",
                    "/myconext/api/mobile/oidc/redirect",
                    "/myconext/api/mobile/verify/redirect",
                    "/mobile/api/create-from-mobile-api"
            );
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            String[] antPatterns = {"/myconext/api/eduid/**", "/mobile/**"};
            http
                    .requestMatchers()
                    .antMatchers(antPatterns)
                    .and()
                    .authorizeRequests(authz -> authz
                            .antMatchers("/myconext/api/eduid/eppn").hasAuthority("SCOPE_eduid.nl/eppn")
                            .antMatchers("/myconext/api/eduid/eduid").hasAuthority("SCOPE_eduid.nl/eduid")
                            .antMatchers("/myconext/api/eduid/links").hasAuthority("SCOPE_eduid.nl/links")
                            .antMatchers("/mobile/**").hasAuthority("SCOPE_eduid.nl/mobile")
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(token -> token
                            .introspectionUri(introspectionUri)
                            .introspectionClientCredentials(clientId, secret)));

        }
    }
}


