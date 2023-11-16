package myconext.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "identity-provider-meta-data")
public class IdentityProviderMetaData {

    private String singleSignOnServiceURI;
    private String name;
    private String description;
    private String logoURI;

}
