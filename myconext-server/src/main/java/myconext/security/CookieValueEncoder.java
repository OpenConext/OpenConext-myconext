package myconext.security;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class CookieValueEncoder {

    private final String salt;

    public CookieValueEncoder(@Value("${tiqr_hash_secret}") String salt) {
        this.salt = salt;

    }

    @SneakyThrows
    public String encode(String value) {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
        messageDigest.update(salt.getBytes(StandardCharsets.UTF_8));
        byte[] bytes = messageDigest.digest(value.getBytes(StandardCharsets.UTF_8));
        byte[] encoded = org.apache.commons.codec.binary.Base64.encodeBase64(bytes);
        return new String(encoded);
    }

    public boolean matches(String rawValue, String encoded) {
        return encode(rawValue).equals(encoded);
    }
}
