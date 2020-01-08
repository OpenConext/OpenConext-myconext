package myconext.mail;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import myconext.AbstractIntegrationTest;
import myconext.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.UUID;

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
    public void after() {
        greenMail.stop();
    }

    @Test
    public void sendMagicLink() throws MessagingException {
        String hash = UUID.randomUUID().toString();
        mailBox.sendMagicLink(user("jdoe@example.com"), hash, "http://mock-sp");
        String mail = mailBody();
        assertTrue(mail.contains(hash));
    }

    private String mailBody() throws MessagingException {
        await().until(() -> greenMail.getReceivedMessages().length != 0);
        MimeMessage mimeMessage = greenMail.getReceivedMessages()[0];
        assertEquals("jdoe@example.com", mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());
        return getBody(mimeMessage);
    }
}