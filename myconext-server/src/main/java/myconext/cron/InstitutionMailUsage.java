package myconext.cron;

import com.mongodb.client.MongoClient;
import myconext.mail.MailBox;
import myconext.manage.Manage;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class InstitutionMailUsage extends AbstractNodeLeader {

    public static final String LOCK_NAME =  "institution_mail_usage_lock_name";
    private static final Log LOG = LogFactory.getLog(InstitutionMailUsage.class);
    public static final int INSTITUTION_MAIL_MONTHS = 5;

    private final Manage manage;
    private final MailBox mailBox;
    private final UserRepository userRepository;
    private final boolean mailInstitutionMailUsage;
    private final boolean dryRunEmail;
    private final int mailInstitutionBatchSize;

    @Autowired
    public InstitutionMailUsage(Manage manage,
                                MailBox mailBox,
                                UserRepository userRepository,
                                MongoClient mongoClient,
                                @Value("${mongodb_db}") String databaseName,
                                @Value("${feature.mail_institution_mail_usage}") boolean mailInstitutionMailUsage,
                                @Value("${cron.dry-run-email}") boolean dryRunEmail,
                                @Value("${cron.mail-institution-batch-size:1}") int mailInstitutionBatchSize) {
        super(LOCK_NAME, mongoClient, databaseName);

        this.manage = manage;
        this.mailBox = mailBox;
        this.userRepository = userRepository;
        this.mailInstitutionMailUsage = mailInstitutionMailUsage;
        this.dryRunEmail = dryRunEmail;
        this.mailInstitutionBatchSize = mailInstitutionBatchSize;
    }

    @Scheduled(cron = "${cron.mail-institution-mail-usage-expression}")
    @SuppressWarnings("unchecked")
    public void mailUsersWithInstitutionMail() {
        if (!mailInstitutionMailUsage) {
            return;
        }
        super.perform("InstitutionMailUsage#mailUsersWithInstitutionMail", () -> doMailUsersWithInstitutionMail());
    }

    private void doMailUsersWithInstitutionMail() {
        LOG.info("Starting InstitutionMailUsage job");
        long start = System.currentTimeMillis();
        try {
            List<String> queryList = manage.getDomainNames().stream()
                    .filter(domainName -> !domainName.contains("*") && !domainName.contains("surf"))
                    .map(domain -> Pattern.quote(domain))
                    .toList();
            String regex = "@(" + String.join("|", queryList) + "$)";

            LocalDateTime cutoff = LocalDateTime.now().minusMonths(INSTITUTION_MAIL_MONTHS);

            List<User> users = userRepository.findByEmailRegexAndInstitutionMailSendDateBeforeOrInstitutionMailSendDateIsNull(
                    regex,
                    cutoff,
                    PageRequest.of(0, mailInstitutionBatchSize)
            );

            if (!dryRunEmail) {
                users.forEach(user -> {
                    mailBox.sendInstitutionMailWarning(user);
                    user.setInstitutionMailSendDate(LocalDateTime.now());
                    userRepository.save(user);
                });
            }

            LOG.info(String.format("Mailed %s users who use their institution domain in %s ms, dry-run: %s",
                    users.size(), System.currentTimeMillis() - start, dryRunEmail));
        } catch (Exception e) {
            LOG.error("Error in mailUsersWithInstitutionMail", e);
        }
    }

}
