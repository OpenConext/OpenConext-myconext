package myconext.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import myconext.AbstractMailBoxTest;
import myconext.security.VerificationCodeGenerator;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@TestPropertySource(properties = {
        "gui.disclaimer.content=SomeTestEnvironment",
})
public class MailBoxNotProductionEnvironmentTest extends AbstractMailBoxTest {

    @Autowired
    private MailBox mailBox;

    @Test
    @SneakyThrows
    public void mailBoxTest_emailSubjectUppercaseTestEnvironment() {
        String code = VerificationCodeGenerator.generateOneTimeLoginCode();
        mailBox.sendOneTimeLoginCode(user("jdoe@example.com", "en"), code);

        MimeMessage mimeMessage = mailMessage();
        String subject = mimeMessage.getSubject();
        assertEquals("[SOMETESTENVIRONMENT environment] eduID login code", subject);

        String body = getBody(mimeMessage);
        assertTrue(body.contains(code));
    }
}
