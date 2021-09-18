package myconext.security;

import myconext.config.BeanConfig;
import myconext.crypto.KeyGenerator;
import myconext.log.MDCFilter;
import myconext.manage.ServiceProviderResolver;
import myconext.model.ServiceProvider;
import myconext.repository.UserRepository;
import myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import myconext.shibboleth.ShibbolethUserDetailService;
import myconext.shibboleth.mock.MockShibbolethFilter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.security.saml.key.SimpleKey;
import org.springframework.security.saml.provider.config.RotatingKeys;
import org.springframework.security.saml.provider.identity.config.ExternalServiceProviderConfiguration;
import org.springframework.security.saml.provider.identity.config.SamlIdentityProviderSecurityConfiguration;
import org.springframework.security.saml.provider.identity.config.SamlIdentityProviderSecurityDsl;
import org.springframework.security.saml.saml2.metadata.NameId;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.springframework.security.saml.saml2.signature.AlgorithmMethod.RSA_SHA512;
import static org.springframework.security.saml.saml2.signature.DigestMethod.SHA512;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    private static Log LOG = LogFactory.getLog(SecurityConfiguration.class);

    @Configuration
    @Order(1)
    public static class SamlSecurity extends SamlIdentityProviderSecurityConfiguration {
        private Resource privateKeyPath;
        private Resource certificatePath;
        private List<ServiceProvider> serviceProviders = new ArrayList<>();
        private String idpEntityId;
        private BeanConfig beanConfig;

        public SamlSecurity(BeanConfig beanConfig,
                            @Value("${private_key_path}") Resource privateKeyPath,
                            @Value("${certificate_path}") Resource certificatePath,
                            @Value("${idp_entity_id}") String idpEntityId,
                            @Value("${sp_entity_id}") String spEntityId,
                            @Value("${sp_entity_metadata_url}") String spMetaDataUrl) {
            super("/saml/guest-idp/", beanConfig);
            this.beanConfig = beanConfig;
            this.privateKeyPath = privateKeyPath;
            this.certificatePath = certificatePath;
            this.idpEntityId = idpEntityId;

            List<String> spEntityIdentifiers = commaSeparatedToList(spEntityId);
            List<String> spMetaDataUrls = commaSeparatedToList(spMetaDataUrl);
            for (int i = 0; i < spEntityIdentifiers.size(); i++) {
                serviceProviders.add(new ServiceProvider(spEntityIdentifiers.get(i), spMetaDataUrls.get(i)));
            }
        }

        private List<String> commaSeparatedToList(String spEntityId) {
            return Arrays.stream(spEntityId.split(",")).map(String::trim).collect(toList());
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);

            String prefix = getPrefix();
            SamlIdentityProviderSecurityDsl configurer = new GuestIdentityProviderDsl(beanConfig);

            SamlIdentityProviderSecurityDsl samlIdentityProviderSecurityDsl = http.apply(configurer)
                    .prefix(prefix)
                    .useStandardFilters(false)
                    .entityId(idpEntityId)
                    .alias("guest-idp")
                    .singleLogout(false)
                    .signMetadata(true)
                    .signatureAlgorithms(RSA_SHA512, SHA512)
                    .nameIds(asList(NameId.PERSISTENT))
                    .rotatingKeys(getKeys());
            serviceProviders.forEach(sp -> samlIdentityProviderSecurityDsl.serviceProvider(
                    new ExternalServiceProviderConfiguration()
                            .setAlias(sp.getEntityId())
                            .setMetadata(sp.getMetaDataUrl())
                            .setSkipSslValidation(false)

            ));
        }

        private RotatingKeys getKeys() throws Exception {
            String privateKey;
            String certificate;
            if (this.privateKeyPath.exists() && this.certificatePath.exists()) {
                privateKey = read(this.privateKeyPath);
                certificate = read(this.certificatePath);
            } else {
                String[] keys = KeyGenerator.generateKeys();
                privateKey = keys[0];
                certificate = keys[1];
            }
            return new RotatingKeys()
                    .setActive(
                            new SimpleKey()
                                    .setName("idp-signing-key")
                                    .setPrivateKey(privateKey)
                                    //to prevent null-pointer in SamlKeyStoreProvider
                                    .setPassphrase("")
                                    .setCertificate(certificate)
                    );
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
        private final ServiceProviderResolver serviceProviderResolver;
        private final String mijnEduIDEntityId;

        public InternalSecurityConfigurationAdapter(Environment environment,
                                                    UserRepository userRepository,
                                                    ServiceProviderResolver serviceProviderResolver,
                                                    @Value("${mijn_eduid_entity_id}") String mijnEduIDEntityId) {
            this.environment = environment;
            this.userRepository = userRepository;
            this.serviceProviderResolver = serviceProviderResolver;
            this.mijnEduIDEntityId = mijnEduIDEntityId;
        }

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers("/actuator/**", "/myconext/api/idp/**");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .requestMatchers()
                    .antMatchers("/myconext/api/sp/**", "/startSSO")
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                    .addFilterAfter(new MDCFilter(), FilterSecurityInterceptor.class)
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

            if (environment.acceptsProfiles(Profiles.of("test", "dev"))) {
                //we can't use @Profile, because we need to add it before the real filter
                http.addFilterBefore(new MockShibbolethFilter(), ShibbolethPreAuthenticatedProcessingFilter.class);
            }
        }
    }

    @Configuration
    @Order(2)
    public static class AppSecurity extends WebSecurityConfigurerAdapter {

        @Value("${attribute_aggregation.user}")
        private String attributeAggregationUser;

        @Value("${attribute_aggregation.password}")
        private String attributeAggregationPassword;

        @Value("${attribute_manipulation.user}")
        private String attributeManipulationUser;

        @Value("${attribute_manipulation.password}")
        private String attributeManipulationPassword;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            String[] antPatterns = {
                    "/myconext/api/attribute-aggregation/**",
                    "/myconext/api/attribute-manipulation/**",
                    "/myconext/api/system/**"
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
            auth
                    .inMemoryAuthentication()
                    .withUser(attributeAggregationUser)
                    .password("{noop}" + attributeAggregationPassword)
                    .roles("attribute-aggregation", "system")
                    .and()
                    .withUser(attributeManipulationUser)
                    .password("{noop}" + attributeManipulationPassword)
                    .roles("attribute-manipulation");
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
        protected void configure(HttpSecurity http) throws Exception {
            String[] antPatterns = {"/myconext/api/eduid/**"};
            http
                    .requestMatchers()
                    .antMatchers(antPatterns)
                    .and()
                    .authorizeRequests(authz -> authz
                            .antMatchers("/myconext/api/eduid/eppn").hasAuthority("SCOPE_eduid.nl/eppn")
                            .antMatchers("/myconext/api/eduid/eduid").hasAuthority("SCOPE_eduid.nl/eduid")
                            .antMatchers("/myconext/api/eduid/attributes").hasAuthority("SCOPE_eduid.nl/attributes")
                            .anyRequest().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2.opaqueToken(token -> token
                            .introspectionUri(introspectionUri)
                            .introspectionClientCredentials(clientId, secret)));
        }
    }
}


