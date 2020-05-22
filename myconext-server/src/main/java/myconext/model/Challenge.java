package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "challenges")
@Getter
@NoArgsConstructor
public class Challenge {
    @Id
    private String id;

    private String token;

    private String challenge;

    private String email;

    public Challenge(String token, String challenge) {
        this.token = token;
        this.challenge = challenge;
    }

    public Challenge(String token, String challenge, String email) {
        this.token = token;
        this.challenge = challenge;
        this.email = email;
    }
}
