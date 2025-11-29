package myconext.cron;

import com.mongodb.client.MongoClient;
import myconext.mail.MailBox;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class NudgeAppMail extends AbstractNodeLeader {

    public static final String LOCK_NAME =  "nudge_app_mail_lock_name";
    private static final Log LOG = LogFactory.getLog(NudgeAppMail.class);

    private final MailBox mailBox;
    private final UserRepository userRepository;
    private final boolean nudgeAppMailFeature;
    private final long nudgeAppMailDaysAfterCreation;
    private final boolean dryRunEmail;
    private final boolean useApp;

    @Autowired
    public NudgeAppMail(MailBox mailBox,
                        UserRepository userRepository,
                        MongoClient mongoClient,
                        @Value("${mongodb_db}") String databaseName,
                        @Value("${cron.nudge-app-mail-days-after-creation}") long nudgeAppMailDaysAfterCreation,
                        @Value("${feature.nudge_app_mail}") boolean nudgeAppMailFeature,
                        @Value("${cron.dry-run-email}") boolean dryRunEmail,
                        @Value("${feature.use_app}") boolean useApp) {
        super(LOCK_NAME, mongoClient, databaseName);

        this.mailBox = mailBox;
        this.userRepository = userRepository;
        this.nudgeAppMailDaysAfterCreation = nudgeAppMailDaysAfterCreation;
        this.nudgeAppMailFeature = nudgeAppMailFeature;
        this.dryRunEmail = dryRunEmail;
        this.useApp = useApp;
    }

    @Scheduled(cron = "${cron.nudge-app-mail-expression}")
    @SuppressWarnings("unchecked")
    public void mailUsersWithoutApp() {
        if (!nudgeAppMailFeature || !useApp) {
            return;
        }
        super.perform("NudgeAppMail#mailUsersWithoutApp", () -> doMailUsersWithoutApp());
    }

    private void doMailUsersWithoutApp() {
        LOG.info("Starting NudgeAppMail job");
        long start = System.currentTimeMillis();
        try {
            long createdBefore = (new Date().getTime() - (nudgeAppMailDaysAfterCreation * 24 * 60 * 60 * 1000)) / 1000;
            List<User> users = userRepository.findByNoEduIDApp(createdBefore);
            if (!dryRunEmail) {
                users.forEach(user -> {
                    mailBox.sendNudgeAppMail(user);
                    user.setNudgeAppMailSend(true);
                    userRepository.save(user);
                });
            }
            LOG.info(String.format("Mailed %s users to nudge the app in %s ms, dry-run: %s",
                    users.size(), System.currentTimeMillis() - start, dryRunEmail));
        } catch (Exception e) {
            LOG.error("Error in mailUsersWithInstitutionMail", e);
        }
    }

}
