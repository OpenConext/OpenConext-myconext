package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import myconext.exceptions.ForbiddenException;
import myconext.security.VerificationCodeGenerator;

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

    private long lastResendAt;

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

    public void checkResendRateLimit(long minIntervalMillis) {
        long now = System.currentTimeMillis();
        if (this.lastResendAt > 0 && now < this.lastResendAt + minIntervalMillis) {
            throw new ForbiddenException("Resend forbidden due to rate limit");
        }
    }

    public void markResent() {
        this.lastResendAt = System.currentTimeMillis();
    }

    public boolean isExpired() {
        return (createdAt + (1000 * 60 * VALIDITY_LOGIN_CODE_MINUTES)) < System.currentTimeMillis();
    }

    public boolean isCodeAlmostExpired() {
        if ((createdAt + (1000 * 60 * 9)) < System.currentTimeMillis()) {
            String newCode = VerificationCodeGenerator.generateOneTimeLoginCode();
            this.code = newCode;
            return true;
        }
        return false;
    }
}
