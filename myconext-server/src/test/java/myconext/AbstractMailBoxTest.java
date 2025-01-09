package myconext;

import com.icegreen.greenmail.junit4.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;
import jakarta.mail.internet.MimeMessage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@SuppressWarnings("deprecation")
public abstract class AbstractMailBoxTest extends AbstractIntegrationTest {

    @Rule
    public final GreenMailRule greenMail =
            new GreenMailRule(new ServerSetup(1025, null, ServerSetup.PROTOCOL_SMTP));

    protected MimeMessage mailMessage() {
        await().atMost(1, TimeUnit.SECONDS).until(() -> greenMail.getReceivedMessages().length != 0);
        return greenMail.getReceivedMessages()[0];
    }

    protected List<MimeMessage> mailMessages() {
        await().atMost(1, TimeUnit.SECONDS).until(() -> greenMail.getReceivedMessages().length != 0);
        return List.of(greenMail.getReceivedMessages());
    }

}