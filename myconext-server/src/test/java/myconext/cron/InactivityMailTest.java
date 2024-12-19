package myconext.cron;

import myconext.AbstractMailBoxTest;
import myconext.model.User;
import myconext.model.UserInactivity;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

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

    private final String DELETED_EMAIL = "DELETED";

    @Autowired
    protected InactivityMail inactivityMail;

    @Test
    public void mailInactivityMail() {
        inactivityUserSeed();

        inactivityMail.mailInactiveUsers();

        List<MimeMessage> mimeMessages = mailMessages();
        assertEquals(4, mimeMessages.size());
        Stream.of(UserInactivity.values()).forEach(userInactivity -> {
            User user = userRepository.findOneUserByEmail(userInactivity.name());
            assertEquals(userInactivity, user.getUserInactivity());
        });
        Optional<User> optionalUser = userRepository.findUserByEmail(DELETED_EMAIL);
        assertFalse(optionalUser.isPresent());
    }

    private void inactivityUserSeed() {
        long oneDayInMillis = 24 * 60 * 60 * 1000L;
        long yesterday = System.currentTimeMillis() - oneDayInMillis;
        Stream.of(UserInactivity.values()).forEach(userInactivity -> {
            User user = new User();
            user.setLastLogin(yesterday - (userInactivity.getInactivityDays() * oneDayInMillis));
            user.setEmail(userInactivity.name());
            user.setUserInactivity(userInactivity.getPreviousUserInactivity());
            userRepository.save(user);
        });
        //And one extra User who is to be deleted
        User user = new User();
        user.setLastLogin(yesterday - (((5L * 365) + 5) * oneDayInMillis));
        user.setEmail(DELETED_EMAIL);
        user.setUserInactivity(UserInactivity.WEEK_1_BEFORE_5_YEARS);
        userRepository.save(user);

    }

}