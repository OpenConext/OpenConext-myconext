package myconext.cron;

import lombok.SneakyThrows;
import myconext.AbstractMailBoxTest;
import myconext.model.User;
import myconext.model.UserInactivity;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static myconext.cron.InactivityMail.ONE_DAY_IN_MILLIS;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
                "verify.base_uri=http://localhost:8098",
                "cron.dry-run-email=true"
        })
public class InactivityMailDryRunTest extends AbstractMailBoxTest {

    @Autowired
    protected InactivityMail inactivityMail;

    @SneakyThrows
    @Test
    public void mailInactivityMail() {
        inactivityUserSeed("en", false);

        inactivityMail.mailInactiveUsers();

        assertThrows(ConditionTimeoutException.class, this::mailMessages);
    }

    @SneakyThrows
    @Test
    public void mailInactivityFirstRun() {
        //See https://github.com/OpenConext/OpenConext-myconext/issues/656
        User user = new User();
        long yesterday = System.currentTimeMillis() - ONE_DAY_IN_MILLIS;
        user.setLastLogin(yesterday - (UserInactivity.WEEK_1_BEFORE_5_YEARS.getInactivityDays() * ONE_DAY_IN_MILLIS));
        user.setEmail(UserInactivity.WEEK_1_BEFORE_5_YEARS.name());
        //Explicit set userInactivity to null for self-explanation of the test code
        user.setUserInactivity(null);
        userRepository.save(user);

        inactivityMail.mailInactiveUsers();

        assertThrows(ConditionTimeoutException.class, this::mailMessages);
    }

}