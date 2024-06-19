package myconext.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceProvider extends RemoteProvider {

    private String homeUrl;

    public ServiceProvider(RemoteProvider remoteProvider, String homeUrl) {
        super(remoteProvider.getEntityId(), remoteProvider.getName(), remoteProvider.getNameNl(), remoteProvider.getInstitutionGuid(), remoteProvider.getLogoUrl());
        this.homeUrl = homeUrl;
    }
}
