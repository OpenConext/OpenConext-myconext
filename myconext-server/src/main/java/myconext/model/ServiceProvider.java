package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceProvider implements Serializable {

    private String entityId;
    private String name;
    private String nameNl;
    private String logoUrl;
    private String homeUrl;
    private String institutionGuid;

}
