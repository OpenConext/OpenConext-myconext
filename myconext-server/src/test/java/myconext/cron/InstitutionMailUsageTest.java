package myconext.cron;

import myconext.AbstractMailBoxTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InstitutionMailUsageTest extends AbstractMailBoxTest {

    @Autowired
    protected InstitutionMailUsage institutionMailUsage;

    @Test
    public void mailUsersWithInstitutionMail() throws MessagingException, IOException {
        institutionMailUsage.mailUsersWithInstitutionMail();

        List<MimeMessage> mimeMessages = mailMessages();

        assertEquals(2, mimeMessages.size());
    }
}