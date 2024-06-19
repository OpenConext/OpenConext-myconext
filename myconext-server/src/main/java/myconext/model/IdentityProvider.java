package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Setter
@Getter
public class IdentityProvider extends RemoteProvider {

    private String institutionBrin;

    public IdentityProvider(RemoteProvider remoteProvider, String institutionBrin) {
        super(remoteProvider.getEntityId(), remoteProvider.getName(), remoteProvider.getNameNl(), remoteProvider.getInstitutionGuid(), remoteProvider.getLogoUrl());
        this.institutionBrin = institutionBrin;
    }
}
