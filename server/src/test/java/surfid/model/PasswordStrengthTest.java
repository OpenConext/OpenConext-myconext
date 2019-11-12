package surfid.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static surfid.validation.PasswordStrength.strongEnough;

public class PasswordStrengthTest {

    @Test
    public void testStrongEnough() {
        assertFalse(strongEnough(null));
        assertFalse(strongEnough("ABCDEFGH"));
        assertFalse(strongEnough("abcdefghijklmn"));

        assertTrue(strongEnough("A1qwerty"));
        assertTrue(strongEnough("abcdefghijklmno"));
    }
}