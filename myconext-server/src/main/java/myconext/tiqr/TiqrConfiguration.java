package myconext.tiqr;

import lombok.Getter;
import lombok.Setter;
import tiqr.org.push.APNSConfiguration;
import tiqr.org.push.GCMConfiguration;

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
    private boolean pushNotificationsEnabled;
    private String eduIdAppBaseUrl;
    private int rateLimitThreshold = 5;

    private APNSConfiguration apns;
    private GCMConfiguration gcm;

}
