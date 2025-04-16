package myconext.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import myconext.exceptions.ForbiddenException;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Getter
@NoArgsConstructor
@ToString
public class OneTimeLoginCode implements Serializable {

    private static final int VALIDITY_LOGIN_CODE_MINUTES = 10;

    @Setter
    private String code;

    private long createdAt;

    private long delay;

    public OneTimeLoginCode(String code) {
        this.code = code;
        this.createdAt = System.currentTimeMillis();
        this.delay = 1000L;
    }

    public boolean attemptOneTimeLoginVerification(String code) {
        long now = System.currentTimeMillis();
        if (now < this.createdAt + this.delay) {
            throw new ForbiddenException("Attempt forbidden due to rate limit");
        }
        // time-constant comparison
        boolean equals = MessageDigest.isEqual(this.code.getBytes(StandardCharsets.UTF_8),
                code.getBytes(StandardCharsets.UTF_8));
        if (equals) {
            return true;
        }
        this.delay = this.delay * 2;
        return false;
    }

    public boolean isExpired() {
        return (createdAt + (1000 * 60 * VALIDITY_LOGIN_CODE_MINUTES)) < System.currentTimeMillis();
    }
}
