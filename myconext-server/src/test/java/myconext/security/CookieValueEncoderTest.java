package myconext.security;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieValueEncoderTest {

    private final CookieValueEncoder cookieValueEncoder = new CookieValueEncoder(UUID.randomUUID().toString());

    @Test
    void encodeAndMatches() {
        String value = "Joe Doe";
        String encoded = cookieValueEncoder.encode(value);

        assertTrue(cookieValueEncoder.matches(value, encoded));
        assertFalse(cookieValueEncoder.matches("nope", encoded));
        assertFalse(new CookieValueEncoder(UUID.randomUUID().toString()).matches(value, encoded));
    }

}