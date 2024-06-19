package myconext.model;

import lombok.Getter;

@Getter
public class IdentityProvider extends RemoteProvider {

    private final String institutionBrin;

    public IdentityProvider(RemoteProvider remoteProvider, String institutionBrin) {
        super(remoteProvider.getEntityId(), remoteProvider.getName(), remoteProvider.getNameNl(), remoteProvider.getInstitutionGuid(), remoteProvider.getLogoUrl());
        this.institutionBrin = institutionBrin;
    }
}
