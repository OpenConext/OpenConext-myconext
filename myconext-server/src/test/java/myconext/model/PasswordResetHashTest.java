package myconext.model;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordResetHashTest {

    @Test
    void isExpired() {
        PasswordResetHash passwordResetHash = new PasswordResetHash(new User(), "hash");
        Date expired = Date.from(LocalDateTime.now().minusHours(2).atZone(ZoneId.systemDefault()).toInstant());
        ReflectionTestUtils.setField(passwordResetHash, "expiresIn", expired);
        assertTrue(passwordResetHash.isExpired());
    }
}