package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;

@Document(collection = "service_providers")
@Getter
@NoArgsConstructor
public class ServiceProvider {

    @Id
    private String id;

    @NotNull
    @Indexed(unique = true)
    private String entityId;

    private String metaDataUrl;

    public ServiceProvider(String entityId) {
        this.entityId = entityId;
    }

    public ServiceProvider(String entityId, String metaDataUrl) {
        this.entityId = entityId;
        this.metaDataUrl = metaDataUrl;
    }
}
