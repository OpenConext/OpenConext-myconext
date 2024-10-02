package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
public class ServiceProvider extends RemoteProvider {

    private String homeUrl;
    @Schema(type = "integer", format = "int64", example = "1634813554997")
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
