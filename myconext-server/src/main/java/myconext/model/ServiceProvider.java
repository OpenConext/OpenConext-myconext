package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ServiceProvider {

    private String entityId;

    private String metaDataUrl;

    public ServiceProvider(String entityId, String metaDataUrl) {
        this.entityId = entityId;
        this.metaDataUrl = metaDataUrl;
    }
}
