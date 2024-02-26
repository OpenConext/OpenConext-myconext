package myconext.security;

import lombok.SneakyThrows;
import myconext.tiqr.TiqrController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class CookieValueEncoder {

    private final String salt;
    private static final Log LOG = LogFactory.getLog(TiqrController.class);


    @SneakyThrows
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
        if (encode(rawValue).equals(encoded)) {
            return true;
        }
        LOG.warn(String.format("Expected tiqr cookie for user %s containing value %s, got %s", rawValue, encode(rawValue), encoded));
        return false;
    }
}
