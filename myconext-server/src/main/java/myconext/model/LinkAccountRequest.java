package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@NoArgsConstructor
@Getter
// Collection name kept for backwards compatibility; shared by all account link flows (mobile, SP and IdP)
@Document(collection = "mobile_link_account_request")
public class LinkAccountRequest implements Serializable {

    @Id
    private String id;

    private String hash;

    private String userId;

    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date expiresIn;

    public LinkAccountRequest(String hash, String userId) {
        this.hash = hash;
        this.userId = userId;
        this.expiresIn = Date.from(LocalDateTime.now().plusMinutes(30).atZone(ZoneId.systemDefault()).toInstant());
    }

}
