package myconext.model;

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
@Document(collection = "password_forgotten_hashes")
public class PasswordForgottenHash implements Serializable {

    @Id
    private String id;

    private String hash;

    private Date expiresIn;

    private String userId;

    public PasswordForgottenHash(User user, String hash) {
        this.userId = user.getId();
        this.hash = hash;
        this.expiresIn = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
    }
}
