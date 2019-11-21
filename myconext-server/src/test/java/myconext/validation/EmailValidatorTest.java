package myconext.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static myconext.validation.EmailValidator.validEmail;

public class EmailValidatorTest {

    @Test
    public void validEmails() {
        assertFalse(validEmail(null));
        assertFalse(validEmail(" "));
        assertFalse(validEmail("a@a.a"));

        assertTrue(validEmail("jdoe@example.com"));
    }
}