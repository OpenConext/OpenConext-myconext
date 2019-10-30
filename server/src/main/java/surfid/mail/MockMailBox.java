package surfid.mail;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.FileCopyUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;

public class MockMailBox extends MailBox {

    public MockMailBox(JavaMailSender mailSender, String emailFrom, String baseUrl) {
        super(mailSender, emailFrom, baseUrl);
    }

    @Override
    protected void doSendMail(MimeMessage message) {
        //nope
    }

    @Override
    protected void setText(String html, MimeMessageHelper helper) throws MessagingException, IOException {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("mac os x")) {
            openInBrowser(html);
        }
    }

    private void openInBrowser(String html) throws IOException {
        File tempFile = File.createTempFile("javamail", ".html");
        FileCopyUtils.copy(html.getBytes(), tempFile);
        Runtime.getRuntime().exec("open " + tempFile.getAbsolutePath());
    }
}
