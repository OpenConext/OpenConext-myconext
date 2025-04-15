package myconext.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import myconext.exceptions.ForbiddenException;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Getter
@NoArgsConstructor
@ToString
public class OneTimeLoginCode implements Serializable {

    @NotBlank
    private String code;

    private long createdAt;

    private long delay;

    public OneTimeLoginCode(String code) {
        this.code = code;
        this.createdAt = System.currentTimeMillis();
        this.delay = 1L;
    }

    // time-constant comparison
    public boolean attemptOneTimeLoginVerification(String code) {
        if ( System.currentTimeMillis() < this.createdAt + this.delay) {
            throw new ForbiddenException("Attempt forbidden due to rate limit");
        }
        boolean equals = MessageDigest.isEqual(this.code.getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8));
        if (equals) {
            return true;
        }
        this.delay = this.delay * 2;
        return false;
    }

}
