package myconext.security;

import lombok.SneakyThrows;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * BCrypt silently truncates any input beyond 72 bytes, which would allow two different long
 * passwords sharing the same first 72 bytes to be treated as identical. To avoid this, any
 * password whose UTF-8 byte representation exceeds that limit is first condensed with SHA-256
 * before being handed to BCrypt, so the full password always contributes to the resulting hash.
 * Passwords within the 72-byte limit are passed through unchanged, so existing BCrypt hashes
 * remain valid without any migration.
 */
public class LongPasswordAwareBCryptPasswordEncoder implements PasswordEncoder {

    private static final int BCRYPT_MAX_BYTES = 72;

    private final BCryptPasswordEncoder delegate;

    public LongPasswordAwareBCryptPasswordEncoder(int strength, SecureRandom random) {
        this.delegate = new BCryptPasswordEncoder(strength, random);
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return delegate.encode(preHashIfNeeded(rawPassword));
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return delegate.matches(preHashIfNeeded(rawPassword), encodedPassword);
    }

    @SneakyThrows
    private CharSequence preHashIfNeeded(CharSequence rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        byte[] bytes = rawPassword.toString().getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= BCRYPT_MAX_BYTES) {
            return rawPassword;
        }
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
        return Base64.getEncoder().encodeToString(digest);
    }
}
