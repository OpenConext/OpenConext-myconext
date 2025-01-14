package myconext.mail;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import myconext.model.ControlCode;
import myconext.model.EmailsSend;
import myconext.model.User;
import myconext.model.UserLogin;
import myconext.repository.EmailsSendRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

import java.io.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class MailBox {

    private static final Log LOG = LogFactory.getLog(MailBox.class);
    private static final String SANITIZE_NAME = "[^\\p{L} '-]";
    private static final List<String> supportedLanguages = List.of("en", "nl");

    private final JavaMailSender mailSender;
    private final String magicLinkUrl;
    private final String mySURFconextURL;
    private final String loginSURFconextURL;
    private final String emailFrom;
    private final Map<String, Map<String, String>> subjects;

    private final MustacheFactory mustacheFactory;
    private final EmailsSendRepository emailsSendRepository;
    private final long emailSpamThresholdSeconds;

    public MailBox(JavaMailSender mailSender,
                   String emailFrom,
                   String magicLinkUrl,
                   String mySURFconextURL,
                   String loginSURFconextURL,
                   ObjectMapper objectMapper,
                   Resource mailTemplatesDirectory,
                   EmailsSendRepository emailsSendRepository,
                   long emailSpamThresholdSeconds) throws IOException {
        this.mailSender = mailSender;
        this.emailFrom = emailFrom;
        this.magicLinkUrl = magicLinkUrl;
        this.mySURFconextURL = mySURFconextURL;
        this.loginSURFconextURL = loginSURFconextURL;
        this.emailsSendRepository = emailsSendRepository;
        this.emailSpamThresholdSeconds = emailSpamThresholdSeconds;
        if (mailTemplatesDirectory.isFile()) {
            LOG.info("Initializing mail templates from file system: " + mailTemplatesDirectory.getFile().getAbsolutePath());
            mustacheFactory = new DefaultMustacheFactory(mailTemplatesDirectory.getFile());
        } else {
            LOG.info("Initializing mail templates from JAR resource: " + mailTemplatesDirectory.getFilename());
            mustacheFactory = new DefaultMustacheFactory(mailTemplatesDirectory.getFilename());
        }
        this.subjects = objectMapper.readValue(inputStream(mailTemplatesDirectory), new TypeReference<>() {
        });
    }

    public void sendMagicLink(User user, String hash, String requesterId) {
        String title = this.getTitle("magic_link", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("destination", requesterId);
        variables.put("hash", hash);
        variables.put("magicLinkUrl", magicLinkUrl);
        sendMail("magic_link", title, variables, preferredLanguage(user), user.getEmail(), true);
    }

    public void sendAccountVerification(User user, String hash) {
        doSendAccountVerification(user, hash, magicLinkUrl);
    }

    public void sendAccountVerificationCreateFromInstitution(User user, String hash, String linkUrl) {
        doSendAccountVerification(user, hash, linkUrl);
    }

    public void sendAccountVerificationMobileAPI(User user, String hash, String linkUrl) {
        doSendAccountVerification(user, hash, linkUrl);
    }

    private void doSendAccountVerification(User user, String hash, String linkUrl) {
        String title = this.getTitle("account_verification", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("hash", hash);
        variables.put("magicLinkUrl", linkUrl);
        sendMail("account_verification", title, variables, preferredLanguage(user), user.getEmail(), true);

    }

    public void sendAccountConfirmation(User user) {
        String title = this.getTitle("account_confirmation", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mySURFconextURL);
        sendMail("account_confirmation", title, variables, preferredLanguage(user), user.getEmail(), false);
    }

    public void sendInstitutionMailWarning(User user) {
        String title = this.getTitle("institution_mail_warning", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mySURFconextURL);
        sendMail("institution_mail_warning", title, variables, preferredLanguage(user), user.getEmail(), false);
    }

    public void sendUserInactivityMail(User user, Map<String, String> localeVariables, boolean firstTwoWarnings) {
        String title = this.getTitle(firstTwoWarnings ? "inactivity_warning_years_ahead" : "inactivity_warning_short_term", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mySURFconextURL);
        variables.putAll(localeVariables);
        String templateName = firstTwoWarnings ? "inactivity_warning_years_ahead" : "inactivity_warning_short_term";
        sendMail(templateName, title, variables, preferredLanguage(user), user.getEmail(), false);
    }

    public void sendNudgeAppMail(User user) {
        String title = this.getTitle("nudge_eduid_app", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mySURFconextURL);
        sendMail("nudge_eduid_app", title, variables, preferredLanguage(user), user.getEmail(), false);
    }

    public void sendResetPassword(User user, String hash, boolean mobileRequest) {
        String title = this.getTitle("reset_password", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mobileRequest ? loginSURFconextURL : mySURFconextURL);
        variables.put("mobileContext", mobileRequest ? "client/mobile/" : "");
        variables.put("hash", hash);
        sendMail("reset_password", title, variables, preferredLanguage(user), user.getEmail(), false);
    }

    public void sendAddPassword(User user, String hash, boolean mobileRequest) {
        String title = this.getTitle("add_password", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mobileRequest ? loginSURFconextURL : mySURFconextURL);
        variables.put("mobileContext", mobileRequest ? "client/mobile/" : "");
        variables.put("hash", hash);
        sendMail("add_password", title, variables, preferredLanguage(user), user.getEmail(), false);
    }

    public void sendNewDevice(User user, UserLogin userLogin) {
        String title = this.getTitle("new_device", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mySURFconextURL);
        variables.put("ipAddress", userLogin.getLookupAddress());
        variables.put("ipLocation", userLogin.getIpLocation());
        sendMail("new_device", title, variables, preferredLanguage(user), user.getEmail(), false);
    }

    public void sendUpdateEmail(User user, String newMail, String hash, boolean mobileRequest) {
        String title = this.getTitle("update_email", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mobileRequest ? loginSURFconextURL : mySURFconextURL);
        variables.put("mobileContext", mobileRequest ? "client/mobile/" : "");
        variables.put("hash", hash);
        sendMail("update_email", title, variables, preferredLanguage(user), newMail, false);
    }

    public void sendUpdateConfirmationEmail(User user, String oldEmail, String newEmail, boolean mobileRequest) {
        String title = this.getTitle("confirmation_update_email", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("oldEmail", oldEmail);
        variables.put("newEmail", newEmail);
        variables.put("mySurfConextURL", mobileRequest ? loginSURFconextURL : mySURFconextURL);
        variables.put("mobileContext", mobileRequest ? "client/mobile/" : "");
        sendMail("confirmation_update_email", title, variables, preferredLanguage(user), oldEmail, false);
        sendMail("confirmation_update_email", title, variables, preferredLanguage(user), newEmail, false);
    }

    public void sendVerificationCode(User user, String verificationCode) {
        String title = this.getTitle("verification_code", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mySURFconextURL);
        variables.put("verificationCode", verificationCode);
        sendMail("verification_code", title, variables, preferredLanguage(user), user.getEmail(), true);
    }

    public void sendControlCode(User user, ControlCode controlCode) {
        //TODO Implement
    }

    private Map<String, Object> variables(User user, String title) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("title", title);
        String fullName = user.getGivenName() + " " + user.getFamilyName();
        String fullNameSanitized = fullName.replaceAll(SANITIZE_NAME, "");
        variables.put("name", fullNameSanitized);
        return variables;
    }

    @SneakyThrows
    private void sendMail(String templateName, String subject, Map<String, Object> variables, String language, String to, boolean checkSpam) {
        if (checkSpam) {
            Optional<EmailsSend> byEmail = emailsSendRepository.findByEmail(to);
            if (byEmail.isPresent() && byEmail.get().getSendAt().toInstant().isAfter(Instant.now().minus(emailSpamThresholdSeconds, ChronoUnit.SECONDS))) {
                throw new IllegalArgumentException(String.format("Not sending email to %s because email was already send at %s", to, byEmail.get().getSendAt()));
            }
        }

        String html = this.mailTemplate(String.format("%s_%s.html", templateName, language), variables);
        String text = this.mailTemplate(String.format("%s_%s.txt", templateName, language), variables);

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setSubject(subject);
        helper.setTo(to);
        setText(html, text, helper);
        helper.setFrom(emailFrom);
        doSendMail(mimeMessage);
    }

    protected void setText(String html, String text, MimeMessageHelper helper) throws jakarta.mail.MessagingException {
        helper.setText(text, html);
    }

    protected void doSendMail(MimeMessage message) {
        new Thread(() -> mailSender.send(message)).start();
    }

    @SneakyThrows
    private String mailTemplate(String name, Map<String, Object> context) {
        return mustacheFactory.compile(name).execute(new StringWriter(), context).toString();
    }

    private String getTitle(String templateName, User user) {
        return this.subjects.get(templateName).get(preferredLanguage(user));
    }

    private String preferredLanguage(User user) {
        String preferredLanguage = user.getPreferredLanguage();
        String localeLanguage = StringUtils.hasText(preferredLanguage) ? preferredLanguage.toLowerCase(Locale.ROOT) : LocaleContextHolder.getLocale().getLanguage();
        return (StringUtils.hasText(localeLanguage) && supportedLanguages.contains(localeLanguage)) ? localeLanguage.toLowerCase(Locale.ROOT) : "en";
    }

    @SneakyThrows
    private InputStream inputStream(Resource mailTemplatesDirectory) {
        if (mailTemplatesDirectory.isFile()) {
            FilenameFilter filter = (dir, fileName) -> "subjects.json".equals(fileName);
            File[] files = mailTemplatesDirectory.getFile().listFiles(filter);
            assert files != null;
            File file = files[0];
            return new FileInputStream(file);
        } else {
            return new ClassPathResource(mailTemplatesDirectory.getFilename() + "/" + "subjects.json").getInputStream();
        }
    }
}
