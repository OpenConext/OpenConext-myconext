package myconext.cron;

import myconext.AbstractIntegrationTest;
import myconext.model.*;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ResourceCleanerTest extends AbstractIntegrationTest {

    @Test
    public void cleanNewUsersNotFinishedRegistration() {
        ResourceCleaner resourceCleaner =
                new ResourceCleaner(authenticationRequestRepository, userRepository, passwordForgottenHashRepository, changeEmailHashRepository, true);

        User user = user("mp@example.org");
        long twoDaysAgo = (System.currentTimeMillis() / 1000L) - (2 * 24 * 60 * 60);
        ReflectionTestUtils.setField(user, "created", twoDaysAgo);
        userRepository.save(user);

        long prev = userRepository.count();

        resourceCleaner.clean();

        assertEquals(prev - 1, userRepository.count());
    }

    @Test
    public void cleanPassForgottenHash() {
        ResourceCleaner resourceCleaner =
                new ResourceCleaner(authenticationRequestRepository, userRepository, passwordForgottenHashRepository, changeEmailHashRepository, true);

        PasswordForgottenHash passwordForgottenHash = new PasswordForgottenHash(user("qwert@exp.com"), "1234567890");
        Date twoHoursAgo = Date.from(LocalDateTime.now().minusHours(2).atZone(ZoneId.systemDefault()).toInstant());

        ReflectionTestUtils.setField(passwordForgottenHash, "expiresIn", twoHoursAgo);
        passwordForgottenHashRepository.save(passwordForgottenHash);

        long prev = passwordForgottenHashRepository.count();

        resourceCleaner.clean();

        assertEquals(prev - 1, passwordForgottenHashRepository.count());
    }

    @Test
    public void cleanChangeEmailHash() {
        ResourceCleaner resourceCleaner =
                new ResourceCleaner(authenticationRequestRepository, userRepository, passwordForgottenHashRepository, changeEmailHashRepository, true);

        ChangeEmailHash changeEmailHash = new ChangeEmailHash(user("qwert@exp.com"), "new@email.com" ,"1234567890");
        Date twoHoursAgo = Date.from(LocalDateTime.now().minusHours(2).atZone(ZoneId.systemDefault()).toInstant());

        ReflectionTestUtils.setField(changeEmailHash, "expiresIn", twoHoursAgo);
        changeEmailHashRepository.save(changeEmailHash);

        long prev = changeEmailHashRepository.count();

        resourceCleaner.clean();

        assertEquals(prev - 1, changeEmailHashRepository.count());
    }

    @Test
    public void clean() {
        doTest(true, 0);
    }

    @Test
    public void notClean() {
        doTest(false, 1);
    }

    private void doTest(boolean cronJobResponsible, int deletedAuthenticationRequests) {
        ResourceCleaner resourceCleaner = new ResourceCleaner(authenticationRequestRepository, userRepository, passwordForgottenHashRepository, changeEmailHashRepository, cronJobResponsible);
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