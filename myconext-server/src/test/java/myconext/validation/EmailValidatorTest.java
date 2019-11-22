package myconext.validation;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EmailValidatorTest {

    private EmailValidator subject = new EmailValidator();

    @Test
    public void validEmails() {
        assertFalse(subject.validEmail(null));
        assertFalse(subject.validEmail(" "));
        assertFalse(subject.validEmail("a@a.a"));

        assertTrue(subject.validEmail("jdoe@example.com"));
    }
}