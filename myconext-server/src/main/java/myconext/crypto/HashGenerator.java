package myconext.crypto;

import java.security.SecureRandom;
import java.util.Base64;

public class HashGenerator {

    private final static SecureRandom random = new SecureRandom();

    private HashGenerator() {
    }

    public static String hash() {
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
