package surfid.mail;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.MustacheFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import surfid.model.User;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class MailBox {

    private JavaMailSender mailSender;
    private String magicLinkUrl;
    private String emailFrom;

    private final MustacheFactory mustacheFactory = new DefaultMustacheFactory();

    public MailBox(JavaMailSender mailSender, String emailFrom, String magicLinkUrl) {
        this.mailSender = mailSender;
        this.emailFrom = emailFrom;
        this.magicLinkUrl = magicLinkUrl;
    }

    public void sendMagicLink(User user, String hash) throws MessagingException, IOException {
        String title = "Magic Link";

        Map<String, Object> variables = new HashMap<>();
        variables.put("title", title);
        variables.put("user", user);
        variables.put("hash", hash);
        variables.put("magicLinkUrl", magicLinkUrl);
        sendMail("mail_templates/magic_link.html", title, variables, user.getEmail());
    }

    private void sendMail(String templateName, String subject, Map<String, Object> variables, String... to) throws MessagingException, IOException {
        String html = this.mailTemplate(templateName, variables);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false);
        helper.setSubject(subject);
        helper.setTo(to);
        setText(html, helper);
        helper.setFrom(emailFrom);
        doSendMail(message);
    }

    protected void setText(String html, MimeMessageHelper helper) throws MessagingException, IOException {
        helper.setText(html, true);
    }

    protected void doSendMail(MimeMessage message) {
        new Thread(() -> mailSender.send(message)).start();
    }

    private String mailTemplate(String templateName, Map<String, Object> context) throws IOException {
        return mustacheFactory.compile(templateName).execute(new StringWriter(), context).toString();
    }

}
