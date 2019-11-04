package surfid.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Document(collection = "authentication_requests")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SamlAuthenticationRequest {

    @Id
    private String id;

    private String requestId;

    private String consumerAssertionServiceURL;

    private String relayState;

    private String hash;

    private Date expiresIn;

    private String userId;

    public SamlAuthenticationRequest(String requestId, String consumerAssertionServiceURL, String relayState) {
        this.id = UUID.randomUUID().toString();
        this.requestId = requestId;
        this.consumerAssertionServiceURL = consumerAssertionServiceURL;
        this.relayState = relayState;
        this.expiresIn = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Transient
    public boolean isExpired() {
        return new Date().after(expiresIn);
    }
}
