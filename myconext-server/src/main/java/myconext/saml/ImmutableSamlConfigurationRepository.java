package myconext.saml;

import org.springframework.security.saml.provider.SamlServerConfiguration;
import org.springframework.security.saml.provider.config.LocalProviderConfiguration;
import org.springframework.security.saml.provider.config.SamlConfigurationRepository;

import static java.util.Arrays.asList;
import static org.springframework.util.StringUtils.hasText;

public class ImmutableSamlConfigurationRepository implements SamlConfigurationRepository {

    private SamlServerConfiguration configuration;
    private String basePath;

    public ImmutableSamlConfigurationRepository(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public SamlServerConfiguration getServerConfiguration() {
        return configuration;
    }

    public void setConfiguration(SamlServerConfiguration configuration) {
        this.configuration = configuration;
        for (LocalProviderConfiguration config : asList(configuration.getIdentityProvider(), configuration.getServiceProvider())) {
            if (config != null && !hasText(config.getBasePath())) {
                config.setBasePath(basePath);
            }
        }
    }
}

