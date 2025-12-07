package myconext.cron;

import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import myconext.AbstractMailBoxTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "mongodb_db=surf_id_test",
                "cron.node-cron-job-responsible=true",
                "email_guessing_sleep_millis=1",
                "sp_entity_id=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "sp_entity_metadata_url=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "spring.main.lazy-initialization=true",
                "eduid_api.oidcng_introspection_uri=http://localhost:8098/introspect",
                "cron.service-name-resolver-initial-delay-milliseconds=60000",
                "oidc.base-url=http://localhost:8098/",
                "sso_mfa_duration_seconds=-1000",
                "feature.requires_signed_authn_request=false",
                "feature.deny_disposable_email_providers=false",
                "verify.base_uri=http://localhost:8098",
                "cron.mail-institution-batch-size=1"
        })
public class InstitutionMailUsageTest extends AbstractMailBoxTest {

    @Autowired
    protected InstitutionMailUsage institutionMailUsage;

    @Test
    @SneakyThrows
    public void mailUsersWithInstitutionMail() {
        //Run first batch
        institutionMailUsage.mailUsersWithInstitutionMail();

        List<MimeMessage> mimeMessagesFirstBatch = mailMessages();

        assertEquals(1, mimeMessagesFirstBatch.size());
        assertEquals("jdoe@example.com", mimeMessagesFirstBatch.get(0).getRecipients(Message.RecipientType.TO)[0].toString());

        //Run Second batch
        purgeEmailFromAllMailboxes();

        institutionMailUsage.mailUsersWithInstitutionMail();

        List<MimeMessage> mimeMessagesSecondBatch = mailMessages();

        assertEquals(1, mimeMessagesSecondBatch.size());
        assertEquals("mdoe@example.com", mimeMessagesSecondBatch.get(0).getRecipients(Message.RecipientType.TO)[0].toString());
    }
}