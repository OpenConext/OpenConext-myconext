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
@Document(collection = "change_email_hashes")
public class ChangeEmailHash implements Serializable {

    @Id
    private String id;

    private String hash;

    private Date expiresIn;

    private String userId;

    private String newEmail;

    public ChangeEmailHash(User user, String newEmail, String hash) {
        this.userId = user.getId();
        this.hash = hash;
        this.expiresIn = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
    }
}
