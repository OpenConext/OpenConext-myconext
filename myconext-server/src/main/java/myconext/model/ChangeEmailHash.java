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
@Document(collection = "change_email_hashes")
public class ChangeEmailHash implements Serializable {

    @Id
    private String id;

    private String hash;

    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date expiresIn;

    private String userId;

    private String newEmail;

    public ChangeEmailHash(User user, String newEmail, String hash) {
        this.userId = user.getId();
        this.hash = hash;
        this.newEmail = newEmail;
        this.expiresIn = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
    }
}
