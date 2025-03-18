package myconext.cron;

import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import myconext.AbstractMailBoxTest;
import myconext.model.User;
import myconext.model.UserInactivity;
import org.apache.commons.io.IOUtils;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static myconext.cron.InactivityMail.ONE_DAY_IN_MILLIS;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "mongodb_db=surf_id_test",
                "cron.node-cron-job-responsible=true",
                "email_guessing_sleep_millis=1",
                "sp_entity_id=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "sp_entity_metadata_url=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "spring.main.lazy-initialization=true",
                "eduid_api.oidcng_introspection_uri=http://localhost:8098/introspect",
                "cron.service-name-resolver-initial-delay-milliseconds=60000",
                "oidc.base-url=http://localhost:8098/",
                "sso_mfa_duration_seconds=-1000",
                "feature.requires_signed_authn_request=false",
                "feature.deny_disposable_email_providers=false",
                "verify.base_uri=http://localhost:8098"
        })
public class InactivityMailTest extends AbstractMailBoxTest {

    public static final String DELETED_EMAIL = "DELETED";

    @Autowired
    protected InactivityMail inactivityMail;

    @SneakyThrows
    @Test
    public void mailInactivityMail() {
        inactivityUserSeed("en");

        inactivityMail.mailInactiveUsers();

        List<MimeMessage> mimeMessages = mailMessages();
        assertEquals(4, mimeMessages.size());
        Stream.of(UserInactivity.values()).forEach(userInactivity -> {
            User user = userRepository.findOneUserByEmail(userInactivity.name());
            assertEquals(userInactivity, user.getUserInactivity());
        });
        Optional<User> optionalUser = userRepository.findUserByEmail(DELETED_EMAIL);
        assertFalse(optionalUser.isPresent());

        //Idempotency check
        greenMail.purgeEmailFromAllMailboxes();
        inactivityMail.mailInactiveUsers();
        assertThrows(ConditionTimeoutException.class, this::mailMessages);
    }

    @SneakyThrows
    @Test
    public void mailInactivityFirstRun() {
        //See https://github.com/OpenConext/OpenConext-myconext/issues/656
        User user = new User();
        long now = System.currentTimeMillis();
        long yesterday = now - ONE_DAY_IN_MILLIS;
        user.setLastLogin(yesterday - (UserInactivity.WEEK_1_BEFORE_5_YEARS.getInactivityDays() * ONE_DAY_IN_MILLIS));
        user.setEmail(UserInactivity.WEEK_1_BEFORE_5_YEARS.name());
        //Explicit set userInactivity to null for self-explanation of the test code
        user.setUserInactivity(null);
        userRepository.save(user);

        inactivityMail.mailInactiveUsers();

        List<MimeMessage> mimeMessages = mailMessages();
        assertEquals(1, mimeMessages.size());
        User userFromDB = userRepository.findOneUserByEmail(UserInactivity.WEEK_1_BEFORE_5_YEARS.name());
        assertEquals(UserInactivity.WEEK_1_BEFORE_5_YEARS, userFromDB.getUserInactivity());
        //Ensure users which have received the last warning have a new lastLogin which is one week before the deletion threshold
        long newLastLoginDelta = (UserInactivity.WEEK_1_BEFORE_5_YEARS.getInactivityDays() + 6) * ONE_DAY_IN_MILLIS;
        assertTrue(userFromDB.getLastLogin() >= (now - newLastLoginDelta));
        //Idempotency check
        greenMail.purgeEmailFromAllMailboxes();
        inactivityMail.mailInactiveUsers();
        assertThrows(ConditionTimeoutException.class, this::mailMessages);
    }

    @SneakyThrows
    @Test
    public void mailInactivityMailDutch() {
        inactivityUserSeed("nl");

        inactivityMail.mailInactiveUsers();

        List<MimeMessage> mimeMessages = mailMessages();
        assertEquals(4, mimeMessages.size());
        //Ordering is not stable
        String allContent = mimeMessages.stream().map(this::messageContent).collect(Collectors.joining());
        List.of("1 jaar", "2 jaar", "5 jaar", "1 maand", "1 week")
                .forEach(s -> assertTrue(allContent.contains(s), "Contains " + s));
    }

    @SneakyThrows
    private String messageContent(MimeMessage mimeMessage) {
        return IOUtils.toString(mimeMessage.getInputStream(), Charset.defaultCharset());
    }

}