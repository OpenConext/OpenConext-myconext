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

    private OneTimeLoginCode oneTimeLoginCode;

    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date expiresIn;

    private String userId;

    private String newEmail;

    public ChangeEmailHash(User user, String newEmail, String hash) {
        this(user, newEmail, hash, null);
    }

    public ChangeEmailHash(User user, String newEmail, String hash, OneTimeLoginCode oneTimeLoginCode) {
        this.userId = user.getId();
        this.newEmail = newEmail;
        this.hash = hash;
        this.oneTimeLoginCode = oneTimeLoginCode;
        this.expiresIn = Date.from(LocalDateTime.now().plusMinutes(10).atZone(ZoneId.systemDefault()).toInstant());
    }
}
