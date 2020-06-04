package myconext.cron;

import myconext.AbstractIntegrationTest;
import myconext.model.LinkedAccount;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import static myconext.security.GuestIdpAuthenticationRequestFilter.EDUPERSON_SCOPED_AFFILIATION_SAML;
import static myconext.security.GuestIdpAuthenticationRequestFilter.EDUPERSON_SCOPED_AFFILIATION_VERIFIED_BY_INSTITUTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ResourceCleanerTest extends AbstractIntegrationTest {

    @Test
    public void clean() {
        doTest(true, 0);
    }

    @Test
    public void notClean() {
        doTest(false, 1);
    }

    private void doTest(boolean cronJobResponsible, int deletedAuthenticationRequests) {
        ResourceCleaner resourceCleaner = new ResourceCleaner(authenticationRequestRepository, userRepository, cronJobResponsible);
        doExpireWithFindProperty(SamlAuthenticationRequest.class, "id", "1");
        expireUserLinkedAccount();

        resourceCleaner.clean();

        assertEquals(deletedAuthenticationRequests, authenticationRequestRepository.findAll().size());

        if (cronJobResponsible) {
            User user = userRepository.findOneUserByEmailIgnoreCase("jdoe@example.com");
            assertEquals(0, user.getLinkedAccounts().size());
            assertFalse(user.getAttributes().containsKey(EDUPERSON_SCOPED_AFFILIATION_SAML));
        }
    }

    private void expireUserLinkedAccount() {
        User user = userRepository.findOneUserByEmailIgnoreCase("jdoe@example.com");
        user.getAttributes()
                .computeIfAbsent(EDUPERSON_SCOPED_AFFILIATION_SAML, key -> new ArrayList<>())
                .add(EDUPERSON_SCOPED_AFFILIATION_VERIFIED_BY_INSTITUTION);

        LinkedAccount linkedAccount = user.getLinkedAccounts().get(0);
        Date expiresIn = Date.from(LocalDateTime.now().minusYears(10L).atZone(ZoneId.systemDefault()).toInstant());
        ReflectionTestUtils.setField(linkedAccount, "expiresAt", expiresIn);
        userRepository.save(user);
    }
}