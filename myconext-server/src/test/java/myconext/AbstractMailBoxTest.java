package myconext;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import myconext.model.User;
import myconext.model.UserInactivity;
import org.junit.Rule;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static myconext.cron.InactivityMail.ONE_DAY_IN_MILLIS;
import static myconext.cron.InactivityMailTest.DELETED_EMAIL;
import static org.awaitility.Awaitility.await;

@SuppressWarnings("deprecation")
public abstract class AbstractMailBoxTest extends AbstractIntegrationTest {

    @Rule
    public final GreenMailRule greenMail =
            new GreenMailRule(new ServerSetup(1025, null, ServerSetup.PROTOCOL_SMTP));

    protected MimeMessage mailMessage() {
        await().atMost(1, TimeUnit.SECONDS).until(() -> greenMail.getReceivedMessages().length != 0);
        return greenMail.getReceivedMessages()[0];
    }

    protected List<MimeMessage> mailMessages() {
        await().atMost(1, TimeUnit.SECONDS).until(() -> greenMail.getReceivedMessages().length != 0);
        return List.of(greenMail.getReceivedMessages());
    }

    protected void inactivityUserSeed(String language) {
        long yesterday = System.currentTimeMillis() - ONE_DAY_IN_MILLIS;
        Stream.of(UserInactivity.values()).forEach(userInactivity -> {
            User user = new User();
            user.setLastLogin(yesterday - (userInactivity.getInactivityDays() * ONE_DAY_IN_MILLIS));
            user.setEmail(userInactivity.name());
            user.setPreferredLanguage(language);
            user.setUserInactivity(userInactivity.getPreviousUserInactivity());
            userRepository.save(user);
        });
        //And one extra User who is to be deleted
        User user = new User();
        user.setLastLogin(yesterday - (((5L * 365) + 5) * ONE_DAY_IN_MILLIS));
        user.setEmail(DELETED_EMAIL);
        user.setUserInactivity(UserInactivity.WEEK_1_BEFORE_5_YEARS);
        userRepository.save(user);

    }


}