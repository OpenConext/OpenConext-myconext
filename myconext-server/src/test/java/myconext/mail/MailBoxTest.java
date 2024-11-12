package myconext.mail;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;
import lombok.SneakyThrows;
import myconext.AbstractIntegrationTest;
import myconext.model.EmailsSend;
import myconext.model.User;
import org.apache.commons.mail.util.MimeMessageParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ActiveProfiles(value = "prod", inheritProfiles = false)
@SuppressWarnings("deprecation")
public class MailBoxTest extends AbstractIntegrationTest {

    @Autowired
    private MailBox mailBox;

    @Rule
    public final GreenMailRule greenMail =
            new GreenMailRule(new ServerSetup(1025, null, ServerSetup.PROTOCOL_SMTP));

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

    @Test(expected = IllegalArgumentException.class)
    public void preventSpam() {
        String email = "jdoe@qwerty.com";
        EmailsSend emailsSend = new EmailsSend(email);
        emailsSendRepository.save(emailsSend);

        String hash = UUID.randomUUID().toString();
        mailBox.sendMagicLink(user(email.toUpperCase(), "en"), hash, "http://mock-sp");
    }

    @SneakyThrows
    @Test
    public void errorMail() {
        mailBox.sendErrorMail(Map.of("error","unexpected"),user("jdoe@examplee.com", "en"));
        MimeMessage mimeMessage = mailMessage();
        assertEquals("info@surfconext.nl", mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());

        String body = getBody(mimeMessage);
        assertTrue(body.contains("Unexpected error occurred"));
        assertTrue(body.contains("user John Doe"));
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
    public void sendResetPassword() throws Exception {
        User user = user("jdoe@example.com");
        mailBox.sendResetPassword(user, "hash", false);

        MimeMessage mimeMessage = mailMessage();
        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
        parser.parse();

        String htmlContent = parser.getHtmlContent();
        assertTrue(htmlContent.contains("http://localhost:3001/reset-password?h=hash"));
    }

    @Test
    public void sendResetPasswordMobile() throws Exception {
        User user = user("jdoe@example.com");
        mailBox.sendResetPassword(user, "hash", true);

        MimeMessage mimeMessage = mailMessage();
        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
        parser.parse();

        String htmlContent = parser.getHtmlContent();
        assertTrue(htmlContent.contains("http://localhost:3000/client/mobile/reset-password?h=hash"));
    }

    @Test
    public void sendUpdateEmail() throws Exception {
        User user = user("jdoe@example.com");
        mailBox.sendUpdateEmail(user, "new@example.com", "hash", false);

        MimeMessage mimeMessage = mailMessage();
        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
        parser.parse();

        String htmlContent = parser.getHtmlContent();
        assertTrue(htmlContent.contains("http://localhost:3001/update-email?h=hash"));
    }

    @Test
    public void sendUpdateEmailMobile() throws Exception {
        User user = user("jdoe@example.com");
        mailBox.sendUpdateEmail(user, "new@example.com", "hash", true);

        MimeMessage mimeMessage = mailMessage();
        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
        parser.parse();

        String htmlContent = parser.getHtmlContent();
        assertTrue(htmlContent.contains("http://localhost:3000/client/mobile/update-email?h=hash"));
    }

    @Test
    public void sendAddPassword() throws Exception {
        User user = user("jdoe@example.com");
        mailBox.sendAddPassword(user, "hash", false);

        MimeMessage mimeMessage = mailMessage();
        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
        parser.parse();

        String htmlContent = parser.getHtmlContent();
        assertTrue(htmlContent.contains("http://localhost:3001/add-password?h=hash"));
    }

    @Test
    public void sendAddPasswordMobile() throws Exception {
        User user = user("jdoe@example.com");
        mailBox.sendAddPassword(user, "hash", true);

        MimeMessage mimeMessage = mailMessage();
        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
        parser.parse();

        String htmlContent = parser.getHtmlContent();
        assertTrue(htmlContent.contains("http://localhost:3000/client/mobile/add-password?h=hash"));
    }

    @Test
    public void sendUpdateConfirmationEmail() throws Exception {
        User user = user("jdoe@example.com");
        mailBox.sendUpdateConfirmationEmail(user, "old@example.com", "new@example.com", false);

        MimeMessage mimeMessage = mailMessage();
        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
        parser.parse();

        String htmlContent = parser.getHtmlContent();
        assertTrue(htmlContent.contains("http://localhost:3001/security"));
    }

    @Test
    public void sendUpdateConfirmationEmailMobile() throws Exception {
        User user = user("jdoe@example.com");
        mailBox.sendUpdateConfirmationEmail(user, "old@example.com", "new@example.com", true);

        MimeMessage mimeMessage = mailMessage();
        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
        parser.parse();

        String htmlContent = parser.getHtmlContent();
        assertTrue(htmlContent.contains("http://localhost:3000/client/mobile/security"));
    }

    @Test
    public void mustacheDefaultEncoding() throws Exception {
        nameEscapeTest("<script>alert()", "</script>", "scriptalert script");
    }

    @Test
    public void preventLink() throws Exception {
        nameEscapeTest("https://föóäërg", "- jj'", "httpsföóäërg - jj&#39;");
    }

    @Test
    public void defaultLocale() throws Exception {
        User user = user("jdoe@example.com");
        user.setPreferredLanguage("pl");
        mailBox.sendAddPassword(user, "hash", false);

        MimeMessage mimeMessage = mailMessage();
        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
        parser.parse();

        String htmlContent = parser.getHtmlContent();
        assertTrue(htmlContent.contains("Someone just requested"));
    }

    private void nameEscapeTest(String givenName, String familyName, String expected) throws Exception {
        User user = user("jdoe@example.com");
        user.setGivenName(givenName);
        user.setFamilyName(familyName);
        mailBox.sendMagicLink(user, "hash", "requesterId");

        MimeMessage mimeMessage = mailMessage();
        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
        parser.parse();

        String htmlContent = parser.getHtmlContent();
        Matcher matcher = Pattern.compile("Hi <strong>(.+?)</strong>").matcher(htmlContent);
        matcher.find();
        String group = matcher.group(1);
        assertEquals(expected, group);
    }

    private MimeMessage mailMessage() {
        await().until(() -> greenMail.getReceivedMessages().length != 0);
        return greenMail.getReceivedMessages()[0];
    }
}