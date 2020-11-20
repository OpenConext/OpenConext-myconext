package myconext.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.exceptions.ForbiddenException;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class EmailDomainGuardTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void isAllowed() throws IOException {
        EmailDomainGuard guard = enabledEmailDomainGuard();

        doIsAllowed(guard, "john@example.com", false);
        doIsAllowed(guard, "john@aaaastrange.me", false);

        doIsAllowed(guard, "john@strange.me", true);
        doIsAllowed(guard, " john@strange.me ", true);
        doIsAllowed(guard, "john@subdomain.strange.me", true);

        assertEquals(2, guard.getAllowedDomains().size());
    }

    @Test
    public void isAllowedDisabled() throws IOException {
        EmailDomainGuard guard = disabledEmailDomainGuard();

        doIsAllowed(guard, "john@example.com", true);
        assertEquals(0, guard.getAllowedDomains().size());
    }

    @Test
    public void schacHomeOrganizationByDomainDisabled() throws IOException {
        EmailDomainGuard guard = disabledEmailDomainGuard();
        assertEquals("eduid.nl", guard.schacHomeOrganizationByDomain("eduid.nl", "john@strange.me"));
    }

    @Test
    public void schacHomeOrganizationByDomainEnabled() throws IOException {
        EmailDomainGuard guard = enabledEmailDomainGuard();
        assertEquals("strange.org", guard.schacHomeOrganizationByDomain("eduid.nl", "john@strange.me"));
        assertEquals("strange.org", guard.schacHomeOrganizationByDomain("eduid.nl", " JOHN@SUBDOMEIN.STRANGE.ME "));
        assertEquals("eduid.nl", guard.schacHomeOrganizationByDomain("eduid.nl", "john@unknown.me"));
    }

    private EmailDomainGuard enabledEmailDomainGuard() throws IOException {
        return new EmailDomainGuard(true, new ClassPathResource("/deny-allow/allowed.json"), objectMapper);
    }

    private EmailDomainGuard disabledEmailDomainGuard() throws IOException {
        return new EmailDomainGuard(false, new ClassPathResource("/deny-allow/allowed.json"), null);
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