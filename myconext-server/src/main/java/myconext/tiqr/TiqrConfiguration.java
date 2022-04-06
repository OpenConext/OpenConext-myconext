package myconext.tiqr;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TiqrConfiguration {

    private String encryptionSecret;
    private String baseUrl;
    private String displayName;
    private String identifier;
    private String version;
    private String logoUrl;
    private String infoUrl;

}
