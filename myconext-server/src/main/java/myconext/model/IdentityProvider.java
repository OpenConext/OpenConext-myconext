package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Setter
@Getter
@ToString
public class IdentityProvider extends RemoteProvider {

    private String domainName;
    private String institutionBrin;

    public IdentityProvider(RemoteProvider remoteProvider, String institutionBrin, String domainName) {
        super(remoteProvider.getEntityId(), remoteProvider.getName(), remoteProvider.getNameNl(), remoteProvider.getInstitutionGuid(), remoteProvider.getLogoUrl());
        this.institutionBrin = institutionBrin;
        this.domainName = domainName;
    }
}
