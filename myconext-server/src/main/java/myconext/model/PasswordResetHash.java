package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@NoArgsConstructor
@Getter
@Document(collection = "password_reset_hashes")
public class PasswordResetHash implements Serializable {

    @Id
    private String id;

    private String hash;

    private OneTimeLoginCode oneTimeLoginCode;

    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date expiresIn;

    private String userId;

    public PasswordResetHash(User user, String hash) {
        this(user, hash, null);
    }

    public PasswordResetHash(User user, String hash, OneTimeLoginCode oneTimeLoginCode) {
        this.userId = user.getId();
        this.hash = hash;
        this.oneTimeLoginCode = oneTimeLoginCode;
        this.expiresIn = Date.from(LocalDateTime.now().plusMinutes(10).atZone(ZoneId.systemDefault()).toInstant());
    }

    @Transient
    public boolean isExpired() {
        return this.expiresIn.before(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
    }
}
