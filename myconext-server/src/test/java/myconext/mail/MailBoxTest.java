package myconext.mail;

import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import myconext.AbstractMailBoxTest;
import myconext.model.EmailsSend;
import myconext.model.IdpScoping;
import myconext.model.User;
import myconext.remotecreation.UpdateExternalEduID;
import myconext.security.VerificationCodeGenerator;
import org.apache.commons.mail2.jakarta.util.MimeMessageParser;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MailBoxTest extends AbstractMailBoxTest {

    @Autowired
    private MailBox mailBox;

    @Test
    public void sendOneTimeLoginCode() {
        doSendOneTimeLoginCode("eduID login code: %s", "en");
    }

    @Test
    public void sendOneTimeLoginCodeNl() {
        doSendOneTimeLoginCode("eduID login code: %s", "nl");
    }

    @Test(expected = IllegalArgumentException.class)
    public void preventSpam() {
        String email = "jdoe@qwerty.com";
        EmailsSend emailsSend = new EmailsSend(email);
        emailsSendRepository.save(emailsSend);

        String code = VerificationCodeGenerator.generateOneTimeLoginCode();
        mailBox.sendOneTimeLoginCode(user(email.toUpperCase(), "en"), code);
    }

    @SneakyThrows
    @Test
    public void errorMail() {
        mailBox.sendErrorMail(Map.of("error", "unexpected"), user("jdoe@examplee.com", "en"));
        MimeMessage mimeMessage = mailMessage();
        assertEquals("info@surfconext.nl", mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());

        String body = getBody(mimeMessage);
        assertTrue(body.contains("Unexpected error occurred"));
        assertTrue(body.contains("user John Doe"));
    }

    @SneakyThrows
    private void doSendOneTimeLoginCode(String expectedSubject, String lang) {
        String code = VerificationCodeGenerator.generateOneTimeLoginCode();
        mailBox.sendOneTimeLoginCode(user("jdoe@example.com", lang), code);

        MimeMessage mimeMessage = mailMessage();
        assertEquals("jdoe@example.com", mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());

        String subject = mimeMessage.getSubject();
        assertEquals(String.format(expectedSubject, code), subject);

        String body = getBody(mimeMessage);
        assertTrue(body.contains(code));
    }

    @Test
    public void sendOneTimeLoginCodeNewUser() {
        doSendOneTimeLoginCodeNewUser("Confirm your eduID email with code: %s", "en");
    }

    @Test
    public void sendOneTimeLoginCodeNewUserNl() {
        doSendOneTimeLoginCodeNewUser("Bevestig je eduID e-mailadres met de code: %s", "nl");
    }

    @SneakyThrows
    private void doSendOneTimeLoginCodeNewUser(String expectedSubject, String lang) {
        String code = VerificationCodeGenerator.generateOneTimeLoginCode();
        mailBox.sendOneTimeLoginCodeNewUser(user("jdoe@examplee.com", lang), code);

        MimeMessage mimeMessage = mailMessage();
        String subject = mimeMessage.getSubject();
        assertEquals(String.format(expectedSubject, code), subject);
    }

    @Test
    public void sendAccountConfirmation() {
        doSendAccountConfirmation("Your eduID has been created", "en");
    }

    @Test
    public void sendAccountConfirmationNl() {
        doSendAccountConfirmation("Je eduID is aangemaakt", "nl");
    }

    @SneakyThrows
    private void doSendAccountConfirmation(String expectedSubject, String lang) {
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
    public void sendUserValidated() throws Exception {
        User user = user("jdoe@example.com");
        UpdateExternalEduID updateExternalEduID = new UpdateExternalEduID();
        updateExternalEduID.setFirstName("Paula Lola");
        updateExternalEduID.setDateOfBirth("01-02-1999");
        mailBox.sendUserValidated(user, updateExternalEduID, IdpScoping.studielink.name());

        MimeMessage mimeMessage = mailMessage();
        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
        parser.parse();

        String htmlContent = parser.getHtmlContent();

        assertTrue(htmlContent.contains("Paula Lola"));
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

}