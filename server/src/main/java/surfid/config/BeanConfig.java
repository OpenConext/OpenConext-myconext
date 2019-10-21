package surfid.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.saml.provider.SamlServerConfiguration;
import org.springframework.security.saml.provider.config.SamlConfigurationRepository;
import org.springframework.security.saml.provider.config.StaticSamlConfigurationRepository;
import org.springframework.security.saml.provider.config.ThreadLocalSamlConfigurationFilter;
import org.springframework.security.saml.provider.config.ThreadLocalSamlConfigurationRepository;
import org.springframework.security.saml.provider.identity.IdpInitiatedLoginFilter;
import org.springframework.security.saml.provider.identity.ResponseEnhancer;
import org.springframework.security.saml.provider.identity.config.SamlIdentityProviderServerBeanConfiguration;
import surfid.repository.UserRepository;
import surfid.security.GuestIdpAuthenticationRequestFilter;

import javax.servlet.Filter;

@Configuration
public class BeanConfig extends SamlIdentityProviderServerBeanConfiguration {

	@Override
	protected SamlServerConfiguration getDefaultHostSamlServerConfiguration() {
		return new SamlServerConfiguration();
	}

	@Override
	public Filter idpAuthnRequestFilter() {
		return new GuestIdpAuthenticationRequestFilter(getSamlProvisioning(), samlAssertionStore());
	}

	@Override
	public ResponseEnhancer samlResponseEnhancer() {
		return response -> {
			return response;
		};
	}

}
