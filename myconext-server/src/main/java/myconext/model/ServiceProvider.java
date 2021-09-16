package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
public class ServiceProvider {

    private String entityId;
    private String metaDataUrl;
    private String name;
    private String nameNl;
    private String logoUrl;
    private String homeUrl;
    private String institutionGuid;

    public ServiceProvider(String entityId, String metaDataUrl) {
        this.entityId = entityId;
        this.metaDataUrl = metaDataUrl;
    }

    public ServiceProvider(String name, String nameNl, String logoUrl, String homeUrl, String institutionGuid) {
        this.name = name;
        this.nameNl = nameNl;
        this.logoUrl = logoUrl;
        this.homeUrl = homeUrl;
        this.institutionGuid = institutionGuid;
    }
}
