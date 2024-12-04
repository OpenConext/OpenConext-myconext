package myconext.cron;

import lombok.Getter;
import myconext.AbstractIntegrationTest;
import myconext.AbstractMailBoxTest;
import myconext.repository.UserRepository;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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