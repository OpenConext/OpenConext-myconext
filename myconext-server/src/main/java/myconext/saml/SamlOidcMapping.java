package myconext.saml;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SamlOidcMapping {

    private String saml;
    private String oidc;
    private boolean multiValue;

}
