package myconext.model;

import myconext.validation.PasswordStrength;
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

        assertTrue(strongEnough("A1qwerty"));
        assertTrue(strongEnough("abcdefghijklmno"));
        assertTrue(strongEnough("Secret123"));
        assertTrue(strongEnough("#!@$%$A1"));
    }

    @Test
    @SuppressWarnings("static-access")
    public void constructor() {
        PasswordStrength passwordStrength = new PasswordStrength();
        assertTrue(passwordStrength.strongEnough("#!@$%$A1"));
    }

}