package myconext.mail;

import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import myconext.AbstractMailBoxTest;
import myconext.model.*;
import myconext.remotecreation.UpdateExternalEduID;
import myconext.security.VerificationCodeGenerator;
import org.apache.commons.mail2.jakarta.util.MimeMessageParser;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.Map;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@TestPropertySource(properties = {
        "email.from_deprovisioning=deprovisioningTest@surfconext.nl",
        "email.from_code=noreplyTest@surfconext.nl",
        "email.from_app_nudge=app-nudgeTest@surfconext.nl",
        "email.from_new_device=new-deviceTest@surfconext.nl",
        "email.error=infoTest@surfconext.nl"

})
public class MailBoxTest extends AbstractMailBoxTest {

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
        String emailErrorMail = env.getProperty("email.error");

        assertEquals("deprovisioningTest@surfconext.nl", emailFromDeprovisioning);
        assertEquals("noreplyTest@surfconext.nl", emailFromCode);
        assertEquals("app-nudgeTest@surfconext.nl", emailFromAppNudge);
        assertEquals("new-deviceTest@surfconext.nl", emailFromNewDevice);
        assertEquals("infoTest@surfconext.nl", emailErrorMail);
    }

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
        assertEquals("infoTest@surfconext.nl", mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());

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
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());

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
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());

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
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    @Test
    public void sendInstitutionMailWarning() {
        doSendInstitutionMailWarning("Tip: use your personal email address", "en");
    }

    @Test
    public void sendInstitutionMailWarningNL() {
        doSendInstitutionMailWarning("Tip: gebruik je persoonlijke e-mailadres", "nl");
    }


    @SneakyThrows
    private void doSendInstitutionMailWarning(String expectedSubject, String lang) {
        mailBox.sendInstitutionMailWarning(user("jdoe@examplee.com", lang));

        MimeMessage mimeMessage = mailMessage();
        assertEquals("app-nudgeTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    @Test
    public void sendResetPassword() throws Exception {
        User user = user("jdoe@example.com");
        mailBox.sendResetPassword(user, "hash", false);

        MimeMessage mimeMessage = mailMessage();
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
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
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
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
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
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
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
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
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
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
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
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
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
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
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
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
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
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
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
        MimeMessageParser parser = new MimeMessageParser(mimeMessage);
        parser.parse();

        String htmlContent = parser.getHtmlContent();
        assertTrue(htmlContent.contains("Someone just requested"));
    }


    @Test
    public void sendAccountVerificationCreateFromInstitution() {
        String code = VerificationCodeGenerator.generateOneTimeLoginCode();
        doSendAccountVerificationCreateFromInstitution(String.format("Confirm your eduID email with code: %s", code), "en", code);
    }

    @Test
    public void sendAccountVerificationCreateFromInstitutionNl() {
        String code = VerificationCodeGenerator.generateOneTimeLoginCode();
        doSendAccountVerificationCreateFromInstitution(String.format("Bevestig je eduID e-mailadres met de code: %s", code), "nl", code);
    }

    @SneakyThrows
    private void doSendAccountVerificationCreateFromInstitution(String expectedSubject, String lang, String code) {

        mailBox.sendAccountVerificationCreateFromInstitution(user("jdoe@examplee.com", lang), code);

        MimeMessage mimeMessage = mailMessage();
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    @Test
    public void sendAccountVerificationMobileAPI() {
        doSendAccountVerificationMobileAPI("Verify your eduID email", "en");
    }

    @Test
    public void sendAccountVerificationMobileAPINl() {
        doSendAccountVerificationMobileAPI("Bevestig je eduID e-mailadres", "nl");
    }

    @SneakyThrows
    private void doSendAccountVerificationMobileAPI(String expectedSubject, String lang) {

        mailBox.sendAccountVerificationMobileAPI(user("jdoe@examplee.com", lang), "hash", "linkUrl");

        MimeMessage mimeMessage = mailMessage();
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    @Test
    public void sendResetPasswordOneTimeCode() {
        String code = VerificationCodeGenerator.generateOneTimeLoginCode();
        doSendResetPasswordOneTimeCode(String.format("Reset your eduID password with code: %s", code), "en", code);
    }

    @Test
    public void sendResetPasswordOneTimeCodeNl() {
        String code = VerificationCodeGenerator.generateOneTimeLoginCode();
        doSendResetPasswordOneTimeCode(String.format("Reset je eduID wachtwoord met de code: %s", code), "nl", code);
    }

    @SneakyThrows
    private void doSendResetPasswordOneTimeCode(String expectedSubject, String lang, String code) {
        mailBox.sendResetPasswordOneTimeCode(user("jdoe@examplee.com", lang), code);

        MimeMessage mimeMessage = mailMessage();
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    @Test
    public void sendChangeEmailOneTimeCode() {
        String code = VerificationCodeGenerator.generateOneTimeLoginCode();
        doSendChangeEmailOneTimeCode(String.format("Update your eduID email with code: %s", code), "en", code);
    }

    @Test
    public void sendChangeEmailOneTimeCodeNl() {
        String code = VerificationCodeGenerator.generateOneTimeLoginCode();
        doSendChangeEmailOneTimeCode(String.format("Verander je eduID email met code: %s", code), "nl", code);
    }

    @SneakyThrows
    private void doSendChangeEmailOneTimeCode(String expectedSubject, String lang, String code) {

        mailBox.sendChangeEmailOneTimeCode(user("jdoe@examplee.com", lang), "newmail@surf.nl", code);

        MimeMessage mimeMessage = mailMessage();
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    @Test
    public void sendServiceDeskControlCode() {
        doSendServiceDeskControlCode("Your verification code to conform your identity", "en");
    }

    @Test
    public void sendServiceDeskControlCodeNl() {
        doSendServiceDeskControlCode("Je verificatie-code om je identiteit te bevestigen", "nl");
    }

    @SneakyThrows
    private void doSendServiceDeskControlCode(String expectedSubject, String lang) {
        String code = VerificationCodeGenerator.generateOneTimeLoginCode();
        mailBox.sendServiceDeskControlCode(user("jdoe@examplee.com", lang), new ControlCode());

        MimeMessage mimeMessage = mailMessage();
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    @Test
    public void sendUserInactivityMail() {
        doSendUserInactivityMail("Keep your eduID up to date", "en");
    }

    @Test
    public void sendUserInactivityMailNl() {
        doSendUserInactivityMail("Houd je eduID up-to-date", "nl");
    }

    @SneakyThrows
    private void doSendUserInactivityMail(String expectedSubject, String lang) {

        mailBox.sendUserInactivityMail(user("jdoe@examplee.com", lang), Collections.emptySortedMap(), true);

        MimeMessage mimeMessage = mailMessage();
        assertEquals("deprovisioningTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    @Test
    public void sendNudgeAppMail() {
        doSendNudgeAppMail("Log in faster and more securely with the eduID app", "en");
    }

    @Test
    public void sendNudgeAppMailNl() {
        doSendNudgeAppMail("Log sneller en veiliger in met de eduID app", "nl");
    }

    @SneakyThrows
    private void doSendNudgeAppMail(String expectedSubject, String lang) {
        mailBox.sendNudgeAppMail(user("jdoe@examplee.com", lang));

        MimeMessage mimeMessage = mailMessage();
        assertEquals("app-nudgeTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    @Test
    public void sendVerificationCode() {
        doSendVerificationCode("eduID verification code", "en");
    }

    @Test
    public void sendVerificationCodeNl() {
        doSendVerificationCode("eduID verificatie code", "nl");
    }

    @SneakyThrows
    private void doSendVerificationCode(String expectedSubject, String lang) {
        String code = VerificationCodeGenerator.generateOneTimeLoginCode();
        mailBox.sendVerificationCode(user("jdoe@examplee.com", lang), code);

        MimeMessage mimeMessage = mailMessage();
        assertEquals("noreplyTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

    @Test
    public void sendNewDevice() {
        doSendNewDevice("New eduID device sign-in", "en");
    }

    @Test
    public void sendNewDeviceNl() {
        doSendNewDevice("Nieuw apparaat ingelogd op eduID", "nl");
    }

    @SneakyThrows
    private void doSendNewDevice(String expectedSubject, String lang) {
        mailBox.sendNewDevice(user("jdoe@examplee.com", lang), new UserLogin());

        MimeMessage mimeMessage = mailMessage();
        assertEquals("new-deviceTest@surfconext.nl", mimeMessage.getFrom()[0].toString());
        String subject = mimeMessage.getSubject();
        assertEquals(expectedSubject, subject);
    }

}

