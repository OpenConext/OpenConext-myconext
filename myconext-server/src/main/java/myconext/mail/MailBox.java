package myconext.mail;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import lombok.SneakyThrows;
import myconext.model.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MailBox {

    private static final Log LOG = LogFactory.getLog(MailBox.class);
    private static final String SANITIZE_NAME = "[^\\p{L} '-]";


    private final JavaMailSender mailSender;
    private final String magicLinkUrl;
    private final String mySURFconextURL;
    private final String emailFrom;
    private final Map<String, Map<String, String>> subjects;

    private final MustacheFactory mustacheFactory;

    public MailBox(JavaMailSender mailSender, String emailFrom, String magicLinkUrl, String mySURFconextURL,
                   ObjectMapper objectMapper, Resource mailTemplatesDirectory) throws IOException {
        this.mailSender = mailSender;
        this.emailFrom = emailFrom;
        this.magicLinkUrl = magicLinkUrl;
        this.mySURFconextURL = mySURFconextURL;
        if (mailTemplatesDirectory.isFile()) {
            LOG.info("Initializing mail templates from file system: " + mailTemplatesDirectory.getFile().getAbsolutePath());
            mustacheFactory = new DefaultMustacheFactory(mailTemplatesDirectory.getFile());
        } else {
            LOG.info("Initializing mail templates from JAR resoruce: " + mailTemplatesDirectory.getFilename());
            mustacheFactory = new DefaultMustacheFactory(mailTemplatesDirectory.getFilename());
        }
        this.subjects = objectMapper.readValue(inputStream("subjects.json", mailTemplatesDirectory), new TypeReference<Map<String, Map<String, String>>>() {
        });
    }

    public void sendMagicLink(User user, String hash, String requesterId) {
        String title = this.getTitle("magic_link", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("destination", requesterId);
        variables.put("hash", hash);
        variables.put("magicLinkUrl", magicLinkUrl);
        sendMail("magic_link", title, variables, preferredLanguage(user), user.getEmail());
    }

    public void sendAccountVerification(User user, String hash) {
        String title = this.getTitle("account_verification", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("hash", hash);
        variables.put("magicLinkUrl", magicLinkUrl);
        sendMail("account_verification", title, variables, preferredLanguage(user), user.getEmail());
    }

    public void sendAccountConfirmation(User user) {
        String title = this.getTitle("account_confirmation", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mySURFconextURL);
        sendMail("account_confirmation", title, variables, preferredLanguage(user), user.getEmail());
    }

    public void sendForgotPassword(User user, String hash) {
        String title = this.getTitle("forgot_password", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mySURFconextURL);
        variables.put("hash", hash);
        sendMail("forgot_password", title, variables, preferredLanguage(user), user.getEmail());
    }

    public void sendUpdateEmail(User user, String newMail, String hash) {
        String title = this.getTitle("update_email", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mySURFconextURL);
        variables.put("hash", hash);
        sendMail("update_email", title, variables, preferredLanguage(user), newMail);
    }

    public void sendUpdateConfirmationEmail(User user, String oldEmail, String newEmail) {
        String title = this.getTitle("confirmation_update_email", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("oldEmail", oldEmail);
        variables.put("newEmail", newEmail);
        variables.put("mySurfConextURL", mySURFconextURL);
        sendMail("confirmation_update_email", title, variables, preferredLanguage(user), oldEmail);
        sendMail("confirmation_update_email", title, variables, preferredLanguage(user), newEmail);
    }

    public void sendVerificationCode(User user, String verificationCode) {
        String title = this.getTitle("verification_code", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("mySurfConextURL", mySURFconextURL);
        variables.put("verificationCode", verificationCode);
        sendMail("verification_code", title, variables, preferredLanguage(user), user.getEmail());
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
    private void sendMail(String templateName, String subject, Map<String, Object> variables, String language, String to) {
        String html = this.mailTemplate(String.format("%s_%s.html", templateName, language), variables);
        String text = this.mailTemplate(String.format("%s_%s.txt", templateName, language), variables);

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setSubject(subject);
            helper.setTo(to);
            setText(html, text, helper);
            helper.setFrom(emailFrom);
            doSendMail(message);
        } catch (MessagingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected void setText(String html, String text, MimeMessageHelper helper) throws MessagingException {
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
        return StringUtils.hasText(preferredLanguage) ? preferredLanguage : LocaleContextHolder.getLocale().getLanguage();
    }

    @SneakyThrows
    private InputStream inputStream(String name, Resource mailTemplatesDirectory) {
        if (mailTemplatesDirectory.isFile()) {
            FilenameFilter filter = (dir, fileName) -> name.equals(fileName);
            File[] files = mailTemplatesDirectory.getFile().listFiles(filter);
            File file = files[0];
            return new FileInputStream(file);
        } else {
            return new ClassPathResource(mailTemplatesDirectory.getFilename() + "/" + name).getInputStream();
        }
    }
}
