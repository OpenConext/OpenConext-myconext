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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InstitutionMailUsage extends AbstractNodeLeader {

    public static final String LOCK_NAME =  "institution_mail_usage_lock_name";
    private static final Log LOG = LogFactory.getLog(InstitutionMailUsage.class);

    private final Manage manage;
    private final MailBox mailBox;
    private final UserRepository userRepository;
    private final boolean mailInstitutionMailUsage;
    private final boolean dryRunEmail;

    @Autowired
    public InstitutionMailUsage(Manage manage,
                                MailBox mailBox,
                                UserRepository userRepository,
                                MongoClient mongoClient,
                                @Value("${mongodb_db}") String databaseName,
                                @Value("${feature.mail_institution_mail_usage}") boolean mailInstitutionMailUsage,
                                @Value("${cron.dry-run-email}") boolean dryRunEmail) {
        super(LOCK_NAME, mongoClient, databaseName);

        this.manage = manage;
        this.mailBox = mailBox;
        this.userRepository = userRepository;
        this.mailInstitutionMailUsage = mailInstitutionMailUsage;
        this.dryRunEmail = dryRunEmail;
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
                    .map(domain -> domain.replace(".", "\\."))
                    .toList();
            String regex = "@" + String.join("|", queryList) + "$";
            List<User> users = userRepository.findByEmailDomain(regex);

            if (!dryRunEmail) {
                users.forEach(mailBox::sendInstitutionMailWarning);
            }
            LOG.info(String.format("Mailed %s users who use their institution domain in %s ms, dry-run: %s",
                    users.size(), System.currentTimeMillis() - start, dryRunEmail));
        } catch (Exception e) {
            LOG.error("Error in mailUsersWithInstitutionMail", e);
        }
    }

}
