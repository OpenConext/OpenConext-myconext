package myconext.cron;

import jakarta.mail.internet.MimeMessage;
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
                "verify.base_uri=http://localhost:8098"
        })
public class InstitutionMailUsageTest extends AbstractMailBoxTest {

    @Autowired
    protected InstitutionMailUsage institutionMailUsage;

    @Test
    public void mailUsersWithInstitutionMail() {
        institutionMailUsage.mailUsersWithInstitutionMail();

        List<MimeMessage> mimeMessages = mailMessages();

        assertEquals(2, mimeMessages.size());
    }
}