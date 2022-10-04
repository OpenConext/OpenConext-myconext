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
        ResourceCleaner resourceCleaner = getResourceCleaner(true);

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
        ResourceCleaner resourceCleaner = getResourceCleaner(true);

        PasswordResetHash passwordResetHash = new PasswordResetHash(user("qwert@exp.com"), "1234567890");
        Date twoHoursAgo = Date.from(LocalDateTime.now().minusHours(2).atZone(ZoneId.systemDefault()).toInstant());

        ReflectionTestUtils.setField(passwordResetHash, "expiresIn", twoHoursAgo);
        passwordResetHashRepository.save(passwordResetHash);

        long prev = passwordResetHashRepository.count();

        resourceCleaner.clean();

        assertEquals(prev - 1, passwordResetHashRepository.count());
    }

    @Test
    public void cleanChangeEmailHash() {
        ResourceCleaner resourceCleaner = getResourceCleaner(true);

        ChangeEmailHash changeEmailHash = new ChangeEmailHash(user("qwert@exp.com"), "new@email.com", "1234567890");
        Date twoHoursAgo = Date.from(LocalDateTime.now().minusHours(2).atZone(ZoneId.systemDefault()).toInstant());

        ReflectionTestUtils.setField(changeEmailHash, "expiresIn", twoHoursAgo);
        changeEmailHashRepository.save(changeEmailHash);

        long prev = changeEmailHashRepository.count();

        resourceCleaner.clean();

        assertEquals(prev - 1, changeEmailHashRepository.count());
    }

    @Test
    public void cleanEmailsSend() {
        ResourceCleaner resourceCleaner = getResourceCleaner(true);

        String email = "jdoe@qwerty.com";
        EmailsSend emailsSend = new EmailsSend(email);
        Date oneMinuteAgo = Date.from(LocalDateTime.now().minusMinutes(2).atZone(ZoneId.systemDefault()).toInstant());

        ReflectionTestUtils.setField(emailsSend, "sendAt", oneMinuteAgo);
        emailsSendRepository.save(emailsSend);

        long prev = emailsSendRepository.count();

        resourceCleaner.clean();

        assertEquals(prev - 1, emailsSendRepository.count());
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
        ResourceCleaner resourceCleaner = getResourceCleaner(cronJobResponsible);
        doExpireWithFindProperty(SamlAuthenticationRequest.class, "id", "1");
        expireUserLinkedAccount();

        resourceCleaner.clean();

        assertEquals(deletedAuthenticationRequests, authenticationRequestRepository.findAll().size());

        if (cronJobResponsible) {
            User user = userRepository.findOneUserByEmail("jdoe@example.com");
            assertEquals(0, user.getLinkedAccounts().size());
        }
    }

    private void expireUserLinkedAccount() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");

        user.getLinkedAccounts().forEach(linkedAccount -> {
            Date expiresIn = Date.from(LocalDateTime.now().minusYears(10L).atZone(ZoneId.systemDefault()).toInstant());
            ReflectionTestUtils.setField(linkedAccount, "expiresAt", expiresIn);
        });
        userRepository.save(user);
    }

    private ResourceCleaner getResourceCleaner(boolean cronJobResponsible) {
        return new ResourceCleaner(authenticationRequestRepository, userRepository, passwordResetHashRepository, changeEmailHashRepository, emailsSendRepository, cronJobResponsible);
    }

}