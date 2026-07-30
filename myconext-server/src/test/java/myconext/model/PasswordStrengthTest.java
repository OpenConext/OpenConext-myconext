package myconext.model;

import org.junit.Test;

import static myconext.validation.PasswordStrength.strongEnough;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PasswordStrengthTest {

    @Test
    public void testStrongEnough() {
        assertFalse(strongEnough(null));
        assertFalse(strongEnough("ABCDEFGH"));
        assertFalse(strongEnough("abcdefghijklmn"));
        assertFalse(strongEnough("#!@$%$Aaaa"));

        assertTrue(strongEnough("A1xwerty"));
        assertTrue(strongEnough("abcdefghijklmno"));
        assertTrue(strongEnough("Secret123"));
        assertTrue(strongEnough("#!@$%$A1"));
    }

    @Test
    public void testDeniedWords() {
        assertFalse(strongEnough("Password123"));
        assertFalse(strongEnough("pAsSwOrD123"));
        assertFalse(strongEnough("MyConext2026"));
        assertFalse(strongEnough("A1qwertyPassword"));
        assertFalse(strongEnough("welcomeWELCOME123"));
        assertFalse(strongEnough("UniversiteitLeiden123"));
        assertFalse(strongEnough("MBOUtrecht2026"));
        assertFalse(strongEnough("WindesheimA1"));
    }

}
