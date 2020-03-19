package myconext.mail;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import myconext.AbstractIntegrationTest;
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
        doSendMagicLink("Magic Link to login", "en");
    }

    @Test
    public void sendMagicLinkNl() throws MessagingException {
        doSendMagicLink("Magische link om in te loggen", "nl");
    }

    private void doSendMagicLink(String expectedSubject, String lang) throws MessagingException {
        String hash = UUID.randomUUID().toString();
        mailBox.sendMagicLink(user("jdoe@example.com", lang), hash, "http://mock-sp");

        MimeMessage mimeMessage = mailMessage();
        assertEquals("jdoe@example.com", mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());

        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);

        String body = getBody(mimeMessage);
        assertTrue(body.contains(hash));
    }

    @Test
    public void sendAccountVerification() throws MessagingException {
        doSendAccountVerification("Please verify your email address for your eduID", "en");
    }

    @Test
    public void sendAccountVerificationNl() throws MessagingException {
        doSendAccountVerification("Verifieer je e-mailadres voor je eduID", "nl");
    }

    private void doSendAccountVerification(String expectedSubject, String lang) throws MessagingException {
        String hash = UUID.randomUUID().toString();
        mailBox.sendAccountVerification(user("jdoe@examplee.com", lang), hash);

        MimeMessage mimeMessage = mailMessage();
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    @Test
    public void sendAccountConfirmation() throws MessagingException {
        doSendAccountConfirmation("Your eduID has been created", "en");
    }

    @Test
    public void sendAccountConfirmationNl() throws MessagingException {
        doSendAccountConfirmation("Je eduID is aangemaakt", "nl");
    }

    private void doSendAccountConfirmation(String expectedSubject, String lang) throws MessagingException {
        mailBox.sendAccountConfirmation(user("jdoe@examplee.com", lang));

        MimeMessage mimeMessage = mailMessage();
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    @Test
    public void sendAccountMigration() throws MessagingException {
        doSendAccountMigration("Your Onegini guest account has been migrated to eduID", "en");
    }

    @Test
    public void sendAccountMigrationNl() throws MessagingException {
        doSendAccountMigration("Je Onegini gastaccount is gemigreerd naar eduID", "nl");
    }

    private void doSendAccountMigration(String expectedSubject, String lang) throws MessagingException {
        mailBox.sendAccountMigration(user("jdoe@examplee.com", lang));

        MimeMessage mimeMessage = mailMessage();
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    private MimeMessage mailMessage() {
        await().until(() -> greenMail.getReceivedMessages().length != 0);
        return greenMail.getReceivedMessages()[0];
    }
}