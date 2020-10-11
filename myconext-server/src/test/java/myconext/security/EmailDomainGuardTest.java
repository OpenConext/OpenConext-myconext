package myconext.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.exceptions.ForbiddenException;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class EmailDomainGuardTest {

    @Test
    public void isAllowed() throws IOException {
        EmailDomainGuard guard = new EmailDomainGuard(true, new ClassPathResource("/deny-allow/allowed.json"), new ObjectMapper());

        doIsAllowed(guard, "john@example.com", false);
        doIsAllowed(guard, "john@aaaastrange.me", false);

        doIsAllowed(guard, "john@strange.me", true);
        doIsAllowed(guard, " john@strange.me ", true);
        doIsAllowed(guard, "john@subdomain.strange.me", true);

        assertEquals(2, guard.getAllowedDomains().size());
    }

    @Test
    public void isAllowedDisabled() throws IOException {
        EmailDomainGuard guard = new EmailDomainGuard(false, new ClassPathResource("/does-not-matter"), null);

        doIsAllowed(guard, "john@example.com", true);
        assertEquals(0, guard.getAllowedDomains().size());
    }

    private void doIsAllowed(EmailDomainGuard guard, String email, boolean expectToAllowed) {
        try {
            guard.enforceIsAllowed(email);
            if (!expectToAllowed) {
                fail();
            }
        } catch (ForbiddenException e) {
            if (expectToAllowed) {
                throw e;
            }
        }
    }
}