package myconext.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.validation.PasswordStrength;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordStrengthTest {

    private static final PasswordStrength passwordStrength = new PasswordStrength(new ObjectMapper());

    @Test
    public void testStrongEnough() {
        assertFalse(passwordStrength.strongEnough(null));
        assertFalse(passwordStrength.strongEnough("ABCDEFGH"));
        assertFalse(passwordStrength.strongEnough("abcdefghijklmn"));
        assertFalse(passwordStrength.strongEnough("#!@$%$Aaaa"));

        assertTrue(passwordStrength.strongEnough("A1xwerty"));
        assertTrue(passwordStrength.strongEnough("abcdefghijklmno"));
        assertTrue(passwordStrength.strongEnough("Secret123"));
        assertTrue(passwordStrength.strongEnough("#!@$%$A1"));
    }

    @Test
    public void testTooLong() {
        assertFalse(passwordStrength.tooLong(null));
        assertFalse(passwordStrength.tooLong("a".repeat(72)));
        assertTrue(passwordStrength.tooLong("a".repeat(73)));
        //Multi-byte characters count towards the byte limit, not the character limit
        assertTrue(passwordStrength.tooLong("é".repeat(37)));
    }

    @Test
    public void testDeniedWords() {
        assertFalse(passwordStrength.strongEnough("Password123"));
        assertFalse(passwordStrength.strongEnough("pAsSwOrD123"));
        assertFalse(passwordStrength.strongEnough("MyConext2026"));
        assertFalse(passwordStrength.strongEnough("A1qwertyPassword"));
        assertFalse(passwordStrength.strongEnough("welcomeWELCOME123"));
        assertFalse(passwordStrength.strongEnough("UniversiteitLeiden123"));
        assertFalse(passwordStrength.strongEnough("MBOUtrecht2026"));
        assertFalse(passwordStrength.strongEnough("WindesheimA1"));
    }

}
