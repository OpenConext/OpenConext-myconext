package myconext.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import myconext.AbstractMailBoxTest;
import myconext.model.UserLogin;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@TestPropertySource(properties = {
        "email.from_deprovisioning=",
        "email.from_code=noreplyTest@surfconext.nl",
        "email.from_app_nudge=",
        "email.from_new_device="

})
public class MailBoxFallBackTest extends AbstractMailBoxTest {

    @Autowired
    private MailBox mailBox;


    @Autowired
    ConfigurableEnvironment env;

    @Test
    public void checkEmailProps() {
        String emailFromDeprovisioning = env.getProperty("email.from_deprovisioning");
        String emailFromCode = env.getProperty("email.from_code");
        String emailFromAppNudge = env.getProperty("email.from_app_nudge");
        String emailFromNewDevice = env.getProperty("email.from_new_device");

        assertEquals("", emailFromDeprovisioning);
        assertEquals("noreplyTest@surfconext.nl", emailFromCode);
        assertEquals("", emailFromAppNudge);
        assertEquals("", emailFromNewDevice);
    }

    @Test
    @SneakyThrows
    public void checkDeprovisioningFallBack() {
        mailBox.sendUserInactivityMail(user("jdoe@examplee.com", "en"), Collections.emptySortedMap(), true);
        MimeMessage mimeMessage = mailMessage();
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
    }

    @Test
    @SneakyThrows
    public void checkAppNudgeFallBack() {
        mailBox.sendInstitutionMailWarning(user("jdoe@examplee.com", "en"));
        MimeMessage mimeMessage = mailMessage();
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
    }

    @Test
    @SneakyThrows
    public void checkNewDeviceFallBack() {
        mailBox.sendNewDevice(user("jdoe@examplee.com", "en"), new UserLogin());
        MimeMessage mimeMessage = mailMessage();
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
    }
}

