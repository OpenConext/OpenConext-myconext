package myconext.mail;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import myconext.model.User;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class MailBox {

    private JavaMailSender mailSender;
    private String magicLinkUrl;
    private String mySURFconextURL;
    private String emailFrom;
    private Map<String, Map<String, String>> subjects;

    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    public MailBox(JavaMailSender mailSender, String emailFrom, String magicLinkUrl, String mySURFconextURL,
                   ObjectMapper objectMapper) throws IOException {
        this.mailSender = mailSender;
        this.emailFrom = emailFrom;
        this.magicLinkUrl = magicLinkUrl;
        this.mySURFconextURL = mySURFconextURL;
        this.subjects = objectMapper.readValue(new ClassPathResource("mail_templates/subjects.json").getInputStream(), new TypeReference<Map<String, Map<String, String>>>() {
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

    public void sendVerificationCode(User user, String verificationCode ) {
        String title = this.getTitle("verification_code", user);
        Map<String, Object> variables = variables(user, title);
        variables.put("verificationCode", verificationCode);
        sendMail("verification_code", title, variables, preferredLanguage(user), user.getEmail());
    }

    private Map<String, Object> variables(User user, String title) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("title", title);
        variables.put("name", user.getGivenName() + " " + user.getFamilyName());
        return variables;
    }

    private void sendMail(String templateName, String subject, Map<String, Object> variables, String language, String to) {
        String html = this.mailTemplate(String.format("mail_templates/%s_%s.html", templateName, language), variables);
        String text = this.mailTemplate(String.format("mail_templates/%s_%s.txt", templateName, language), variables);

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

    private String mailTemplate(String templateName, Map<String, Object> context) {
        return mustacheFactory.compile(templateName).execute(new StringWriter(), context).toString();
    }

    private String getTitle(String templateName, User user) {
        return this.subjects.get(templateName).get(preferredLanguage(user));
    }

    private String preferredLanguage(User user) {
        String preferredLanguage = user.getPreferredLanguage();
        return StringUtils.hasText(preferredLanguage) ? preferredLanguage : LocaleContextHolder.getLocale().getLanguage();

    }
}
