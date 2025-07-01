package myconext.security;

import lombok.SneakyThrows;
import myconext.crypto.KeyGenerator;
import myconext.geo.GeoLocation;
import myconext.mail.MailBox;
import myconext.manage.Manage;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.ExternalUserRepository;
import myconext.repository.UserLoginRepository;
import myconext.repository.UserRepository;
import myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import myconext.shibboleth.ShibbolethUserDetailService;
import myconext.shibboleth.mock.MockShibbolethFilter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import saml.model.SAMLConfiguration;
import saml.model.SAMLIdentityProvider;
import saml.model.SAMLServiceProvider;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.security.config.Customizer.withDefaults;

@EnableWebSecurity
@EnableConfigurationProperties
@EnableMethodSecurity
@Configuration
public class SecurityConfiguration {

    private static final Log LOG = LogFactory.getLog(SecurityConfiguration.class);

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }

    @Configuration
    @Order(1)
    @EnableConfigurationProperties(IdentityProviderMetaData.class)
    public static class SamlSecurity {

        private final GuestIdpAuthenticationRequestFilter guestIdpAuthenticationRequestFilter;

        public SamlSecurity(@Value("${private_key_path}") Resource privateKeyPath,
                            @Value("${certificate_path}") Resource certificatePath,
                            @Value("${idp_entity_id}") String idpEntityId,
                            @Value("${sp_entity_id}") String spEntityId,
                            @Value("${sp_entity_metadata_url}") String spMetaDataUrl,
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
                            CookieValueEncoder cookieValueEncoder,
                            SecurityContextRepository securityContextRepository) {
            ACR.initialize(
                    linkedInstitution,
                    validateNames,
                    validateNamesExternal,
                    affiliationStudent,
                    profileMfa
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
                    cookieValueEncoder,
                    securityContextRepository
            );
        }

        @Bean
        public SecurityFilterChain samlSecurityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository) throws Exception {
            http
                    .securityMatcher("/saml/guest-idp/**")
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(conf -> conf.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                    .addFilterBefore(this.guestIdpAuthenticationRequestFilter,
                            AbstractPreAuthenticatedProcessingFilter.class)
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().hasRole("GUEST"))
                    //We need a reference to the securityContextRepository to update the authentication after an InstitutionAdmin invitation accept
                    .securityContext(securityContextConfigurer ->
                            securityContextConfigurer.securityContextRepository(securityContextRepository));

            return http.build();
        }

        private List<String> commaSeparatedToList(String spEntityId) {
            return Arrays.stream(spEntityId.split(",")).map(String::trim).collect(toList());
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

    @Order
    @Configuration
    public static class InternalSecurityConfigurationAdapter {

        public static final String ROLE_GUEST = "ROLE_GUEST";
        public static final String SERVICE_DESK = "SERVICE_DESK";

        private AuthenticationProvider preAuthenticatedAuthenticationProvider() {
            PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
            provider.setPreAuthenticatedUserDetailsService(new ShibbolethUserDetailService());
            return provider;
        }

        @Bean
        public SecurityFilterChain shibbolethSecurityFilterChain(
                HttpSecurity http,
                Environment environment,
                Manage manage,
                UserRepository userRepository,
                ExternalUserRepository externalUserRepository,
                @Value("${mijn_eduid_entity_id}") String mijnEduIDEntityId,
                @Value("${service_desk_roles}") String[] serviceDeskRoles,
                @Value("${service_desk_role_auto_provisioning}") boolean serviceDeskRoleAutoProvisioning,
                @Value("${host_headers.active}") String activeHost,
                @Value("${host_headers.mijn_ediuid}") String mijnEduIDHost,
                @Value("${host_headers.service_desk}") String serviceDeskHost) throws Exception {
            AuthenticationProvider authenticationProvider = preAuthenticatedAuthenticationProvider();
            ProviderManager providerManager = new ProviderManager(authenticationProvider);
            http
                    .securityMatcher(
                            "/myconext/api/sp/**",
                            "/startSSO",
                            "/config",
                            "/tiqr/sp/**",
                            "/myconext/api/servicedesk/**")
                    .csrf(csrf -> csrf.disable())
                    .sessionManagement(smc -> smc.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                    .authorizeHttpRequests(authz -> authz.requestMatchers(
                                    "/config",
                                    "/myconext/api/idp/**",
                                    "/myconext/api/sp/create-from-institution",
                                    "/myconext/api/sp/create-from-institution/**",
                                    "/myconext/api/sp/idin/issuers",
                                    "myconext/api/swagger-ui/**")
                            .permitAll())
                    .authorizeHttpRequests(authz -> authz.requestMatchers(
                            "/myconext/api/servicedesk/**"
                    ).hasAuthority(SERVICE_DESK))
                    .addFilterBefore(
                            new ShibbolethPreAuthenticatedProcessingFilter(
                                    providerManager,
                                    userRepository,
                                    externalUserRepository,
                                    manage,
                                    mijnEduIDEntityId,
                                    List.of(serviceDeskRoles),
                                    mijnEduIDHost,
                                    serviceDeskHost),
                            AbstractPreAuthenticatedProcessingFilter.class)
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().hasRole("GUEST"));
            if (environment.acceptsProfiles(Profiles.of("test", "dev"))) {
                //we can't use @Profile, because we need to add it before the real filter
                http.addFilterBefore(new MockShibbolethFilter(serviceDeskRoleAutoProvisioning, activeHost), ShibbolethPreAuthenticatedProcessingFilter.class);
            }
            return http.build();
        }
    }

    @Configuration
    @Order(2)
    @EnableConfigurationProperties(ExternalApiConfiguration.class)
    public static class AppSecurity {

        private final ExternalApiConfiguration remoteUsers;

        public AppSecurity(ExternalApiConfiguration remoteUsers) {
            this.remoteUsers = remoteUsers;
        }

        @Bean
        public SecurityFilterChain appSecurityFilterChain(HttpSecurity http) throws Exception {
            String[] antPatterns = {
                    "/myconext/api/attribute-aggregation/**",
                    "/myconext/api/attribute-manipulation/**",
                    "/myconext/api/system/**",
                    "/myconext/api/invite/**",
                    "/api/remote-creation/**"
            };
            http.securityMatcher(antPatterns)
                    .securityContext(sc -> sc.requireExplicitSave(false))
                    .csrf(csrf -> csrf.disable())
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(antPatterns).authenticated())
                    .httpBasic(withDefaults())
                    .authenticationProvider(inMemoryAuthenticationProvider())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            return http.build();
        }

        private DaoAuthenticationProvider inMemoryAuthenticationProvider() {
            DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
            authenticationProvider.setUserDetailsService(new ExtendedInMemoryUserDetailsManager(remoteUsers.getRemoteUsers()));
            return authenticationProvider;
        }

    }

    @Configuration
    @Order(3)
    public static class JWTSecurityConfig {

        @Value("${eduid_api.oidcng_introspection_uri}")
        private String introspectionUri;

        @Value("${eduid_api.oidcng_client_id}")
        private String clientId;

        @Value("${eduid_api.oidcng_secret}")
        private String secret;

        @Bean
        public SecurityFilterChain jwtSecurityFilterChain(HttpSecurity http) throws Exception {
            String[] antPatterns = {"/myconext/api/eduid/**", "/mobile/**"};
            http.securityMatcher(antPatterns)
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(authz -> authz
                            .requestMatchers(
                                    "/mobile/api/idp/create",
                                    "/mobile/api/idp/v2/**",
                                    "/myconext/api/mobile/oidc/redirect",
                                    "/myconext/api/mobile/verify/redirect",
                                    "/mobile/api/create-from-mobile-api",
                                    "/mobile/api/create-from-mobile-api/in-app")
                            .permitAll()
                            .requestMatchers("/myconext/api/eduid/eppn").hasAuthority("SCOPE_eduid.nl/eppn")
                            .requestMatchers("/myconext/api/eduid/eduid").hasAuthority("SCOPE_eduid.nl/eduid")
                            .requestMatchers("/myconext/api/eduid/links").hasAuthority("SCOPE_eduid.nl/links")
                            .requestMatchers("/mobile/**").hasAuthority("SCOPE_eduid.nl/mobile")
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(token -> token
                            .introspectionUri(introspectionUri)
                            .introspectionClientCredentials(clientId, secret)));
            return http.build();
        }
    }
}
