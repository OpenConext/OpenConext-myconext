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
public class IdentityProvider implements Serializable {

    private String entityId;
    private String institutionBrin;
    private String institutionGuid;
    private String displayNameEn;
    private String displayNameNl;
    private String logoUrl;

    public IdentityProvider(String displayNameEn, String displayNameNl) {
        this.displayNameEn = displayNameEn;
        this.displayNameNl = displayNameNl;
    }
}
