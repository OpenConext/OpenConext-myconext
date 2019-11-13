package surfid.mail;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import surfid.AbstractIntegrationTest;
import surfid.model.User;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ActiveProfiles(value = "prod", inheritProfiles = false)
public class MailBoxTest extends AbstractIntegrationTest {

    @Autowired
    private MailBox mailBox;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);

    @Before
    public void before() throws Exception {
        super.before();
        greenMail.start();
        greenMail.purgeEmailFromAllMailboxes();
    }

    @After
    public void after() throws Exception {
        greenMail.stop();
    }

    @Test
    public void sendMagicLink() throws IOException, MessagingException {
        mailBox.sendMagicLink(new User("jdoe@example.com", "John", "Doe"), "hash");
        String mail = mailBody();
        assertTrue(mail.contains("<a href=\"http://localhost:8081/saml/guest-idp/magic?h=hash\">Click to login</a>"));
    }

    private String mailBody() throws MessagingException {
        await().until(() -> greenMail.getReceivedMessages().length != 0);
        MimeMessage mimeMessage = greenMail.getReceivedMessages()[0];
        assertEquals("jdoe@example.com", mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());
        return getBody(mimeMessage);
    }
}