package myconext.cron;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.SneakyThrows;
import myconext.AbstractMailBoxTest;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
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
                "cron.mail-institution-batch-size=200"
        })
public class InstitutionMailUsageBatchTest extends AbstractMailBoxTest {

    @Autowired
    @Getter
    protected UserRepository userRepository;

    @Autowired
    protected InstitutionMailUsage institutionMailUsage;

    @Test
    @SneakyThrows
    public void mailUsersWithInstitutionMail() {
        institutionMailUsage.mailUsersWithInstitutionMail();

        List<MimeMessage> mimeMessagesBatch = mailMessages();

        assertEquals(2, mimeMessagesBatch.size());
        assertTrue(mimeMessagesBatch.stream().anyMatch(m -> hasRecipient(m, "jdoe@example.com")));
        assertTrue(mimeMessagesBatch.stream().anyMatch(m -> hasRecipient(m, "mdoe@example.com")));
    }

    @Test
    @SneakyThrows
    public void mailUsersWithInstitutionMailWithDateNotOlderThan5Months() {
        User user = userRepository.findUserByEmail("jdoe@example.com").get();
        user.setInstitutionMailSendDate(LocalDateTime.now());
        userRepository.save(user);

        institutionMailUsage.mailUsersWithInstitutionMail();

        List<MimeMessage> mimeMessagesBatch = mailMessages();

        assertEquals(1, mimeMessagesBatch.size());
        assertTrue(mimeMessagesBatch.stream().anyMatch(m -> hasRecipient(m, "mdoe@example.com")));
    }

    @Test
    @SneakyThrows
    public void mailUsersWithInstitutionMailWithNotExistingDomain() {
        User user = userRepository.findUserByEmail("jdoe@example.com").get();
        user.setInstitutionMailSendDate(null);
        user.setEmail("jdoe@NotExistingDomain.com");
        userRepository.save(user);

        institutionMailUsage.mailUsersWithInstitutionMail();

        List<MimeMessage> mimeMessagesBatch = mailMessages();

        assertEquals(1, mimeMessagesBatch.size());
        assertTrue(mimeMessagesBatch.stream().anyMatch(m -> hasRecipient(m, "mdoe@example.com")));
    }

    @Test
    @SneakyThrows
    public void mailUsersWithInstitutionMailWithDateOlderThan5Months() {
        User john = userRepository.findUserByEmail("jdoe@example.com").get();
        john.setInstitutionMailSendDate(LocalDateTime.now().minusMonths(5));
        userRepository.save(john);

        User mary = userRepository.findUserByEmail("mdoe@example.com").get();
        mary.setInstitutionMailSendDate(LocalDateTime.now());
        userRepository.save(mary);

        institutionMailUsage.mailUsersWithInstitutionMail();

        List<MimeMessage> mimeMessagesBatch = mailMessages();

        assertEquals(1, mimeMessagesBatch.size());
        assertTrue(mimeMessagesBatch.stream().anyMatch(m -> hasRecipient(m, "jdoe@example.com")));
    }

    @Test
    @SneakyThrows
    public void mailUsersWithInstitutionMailWithDateOlderThan5MonthsAndNew() {
        User john = userRepository.findUserByEmail("jdoe@example.com").get();
        john.setInstitutionMailSendDate(LocalDateTime.now().minusMonths(5));
        userRepository.save(john);

        institutionMailUsage.mailUsersWithInstitutionMail();

        List<MimeMessage> mimeMessagesBatch = mailMessages();

        assertEquals(2, mimeMessagesBatch.size());
        assertTrue(mimeMessagesBatch.stream().anyMatch(m -> hasRecipient(m, "jdoe@example.com")));
        assertTrue(mimeMessagesBatch.stream().anyMatch(m -> hasRecipient(m, "mdoe@example.com")));
    }

    private boolean hasRecipient(MimeMessage msg, String email) {

        try {
            return Arrays.stream(msg.getRecipients(Message.RecipientType.TO))
                    .anyMatch(a -> a.toString().equals(email));
        } catch (MessagingException e) {
            return false;
        }

    }
}