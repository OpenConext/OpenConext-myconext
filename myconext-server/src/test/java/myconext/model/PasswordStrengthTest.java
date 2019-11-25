package myconext.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static myconext.validation.PasswordStrength.strongEnough;

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
}