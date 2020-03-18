package myconext.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.FileCopyUtils;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;

public class MockMailBox extends MailBox {

    private Environment env;

    MockMailBox(JavaMailSender mailSender, String emailFrom, String baseUrl, String mySURFconextURL, ObjectMapper objectMapper, Environment env) throws IOException {
        super(mailSender, emailFrom, baseUrl, mySURFconextURL, objectMapper);
        this.env = env;
    }

    @Override
    protected void doSendMail(MimeMessage message) {
        //nope
    }

    @Override
    protected void setText(String html, String text, MimeMessageHelper helper) {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac os x") && !env.acceptsProfiles(Profiles.of("test"))) {
            openInBrowser(html);
        }
    }

    private void openInBrowser(String html) {
        try {
            File tempFile = File.createTempFile("javamail", ".html");
            FileCopyUtils.copy(html.getBytes(), tempFile);
            Runtime.getRuntime().exec("open " + tempFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
