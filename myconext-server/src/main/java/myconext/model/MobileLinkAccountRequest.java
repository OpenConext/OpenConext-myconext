package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@NoArgsConstructor
@Getter
@Document(collection = "mobile_link_account_request")
public class MobileLinkAccountRequest implements Serializable {

    @Id
    private String id;

    private String hash;

    private String userId;

    private Date expiresIn;

    public MobileLinkAccountRequest(String hash, String userId) {
        this.hash = hash;
        this.userId = userId;
        this.expiresIn = Date.from(LocalDateTime.now().plusMinutes(30).atZone(ZoneId.systemDefault()).toInstant());
    }

}
