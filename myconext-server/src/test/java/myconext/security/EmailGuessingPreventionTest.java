package myconext.security;

import org.junit.Test;

import static org.junit.Assert.*;

public class EmailGuessingPreventionTest {

    private EmailGuessingPrevention subject = new EmailGuessingPrevention(0);

    @Test
    public void sanitizeEmail() {
        assertNull(subject.sanitizeEmail(null));
        assertEquals("jdoe@example.com", subject.sanitizeEmail("jdoe@example.com   "));
    }
}