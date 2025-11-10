package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;

@Document(collection = "user_logins")
@Getter
@NoArgsConstructor
public class UserLogin implements Serializable {
    @Id
    private String id;

    private String uuid;

    private String userId;

    private String userAgent;

    private String forwardedFor;

    private String ipAddress;

    @Setter
    private String ipLocation;

    public UserLogin(User user, Map<String, String> headers) {
        this.userId = user.getId();
        this.uuid = UUID.randomUUID().toString();
        this.userAgent = headers.get("user-agent");
        String forwardedForHeader = headers.get("x-forwarded-for");
        if (StringUtils.hasText(forwardedForHeader)) {
            this.forwardedFor = forwardedForHeader.split(",")[0].trim();
            try {
                InetAddress addr = InetAddress.getByName(this.forwardedFor);
                this.ipAddress = addr.getHostName();
            } catch (UnknownHostException e) {
                //Ignore
            }
        } else {
            this.ipAddress = headers.get("ipAddress");
        }
    }

    public String getLookupAddress() {
        return StringUtils.hasText(forwardedFor) ? forwardedFor : ipAddress;
    }
}
