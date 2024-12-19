package myconext;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetup;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;

import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@SuppressWarnings("deprecation")
public abstract class AbstractMailBoxTest extends AbstractIntegrationTest {

    @Rule
    public final GreenMailRule greenMail =
            new GreenMailRule(new ServerSetup(1025, null, ServerSetup.PROTOCOL_SMTP));

    @Before
    public void before() throws Exception {
        super.before();
        greenMail.start();
        greenMail.purgeEmailFromAllMailboxes();
    }

    @After
    public void after() {
        greenMail.stop();
    }

    protected MimeMessage mailMessage() {
        await().atMost(1, TimeUnit.SECONDS).until(() -> greenMail.getReceivedMessages().length != 0);
        return greenMail.getReceivedMessages()[0];
    }

    protected List<MimeMessage> mailMessages() {
        await().atMost(1, TimeUnit.SECONDS).until(() -> greenMail.getReceivedMessages().length != 0);
        return List.of(greenMail.getReceivedMessages());
    }

}