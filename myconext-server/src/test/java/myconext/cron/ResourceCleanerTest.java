package myconext.cron;

import myconext.AbstractIntegrationTest;
import myconext.model.LinkedAccount;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.assertEquals;

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
        }
    }

    private void expireUserLinkedAccount() {
        User user = userRepository.findOneUserByEmailIgnoreCase("jdoe@example.com");

        LinkedAccount linkedAccount = user.getLinkedAccounts().get(0);
        Date expiresIn = Date.from(LocalDateTime.now().minusYears(10L).atZone(ZoneId.systemDefault()).toInstant());
        ReflectionTestUtils.setField(linkedAccount, "expiresAt", expiresIn);
        userRepository.save(user);
    }
}