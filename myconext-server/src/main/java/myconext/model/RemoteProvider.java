package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RemoteProvider implements Serializable {

    private String entityId;
    private String name;
    private String nameNl;
    private String institutionGuid;
    private String logoUrl;

}
