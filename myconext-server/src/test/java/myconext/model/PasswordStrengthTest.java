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

        //Shorter than 8 characters is always too short
        assertFalse(passwordStrength.strongEnough("short12"));

        //8+ characters is strong enough, regardless of MFA
        assertTrue(passwordStrength.strongEnough("eightchr"));
        assertTrue(passwordStrength.strongEnough("fifteencharacte"));

        //No composition rules: no uppercase, digit or special character is required
        assertTrue(passwordStrength.strongEnough("alllowercase"));
        assertTrue(passwordStrength.strongEnough("all lower case"));

        //Whitespace is allowed, including a password of only whitespace
        assertTrue(passwordStrength.strongEnough("               "));

        //Unicode characters count as a single character each, not by their byte length
        assertTrue(passwordStrength.strongEnough("wachtwoordé日本語🎉"));
    }

    @Test
    public void testTooLong() {
        assertFalse(passwordStrength.tooLong(null));
        assertFalse(passwordStrength.tooLong("a".repeat(128)));
        assertTrue(passwordStrength.tooLong("a".repeat(129)));
        //Character count, not byte count, determines the maximum
        assertFalse(passwordStrength.tooLong("é".repeat(128)));
        assertTrue(passwordStrength.tooLong("é".repeat(129)));
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
