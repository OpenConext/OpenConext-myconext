package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
public class ServiceProvider extends RemoteProvider {

    private String homeUrl;
    private Date createdAt = new Date();

    public ServiceProvider(RemoteProvider remoteProvider, String homeUrl) {
        super(remoteProvider.getEntityId(),
                remoteProvider.getName(),
                remoteProvider.getNameNl(),
                remoteProvider.getInstitutionGuid(),
                remoteProvider.getLogoUrl());
        this.homeUrl = homeUrl;
    }
}
