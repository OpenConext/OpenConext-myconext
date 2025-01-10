package myconext.cron;

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

import java.util.Date;
import java.util.List;

@Component
public class NudgeAppMail {

    private static final Log LOG = LogFactory.getLog(NudgeAppMail.class);

    private final MailBox mailBox;
    private final UserRepository userRepository;
    private final boolean nudgeAppMailFeature;
    private final boolean cronJobResponsible;
    private final long nudgeAppMailDaysAfterCreation;

    @Autowired
    public NudgeAppMail(MailBox mailBox,
                        UserRepository userRepository,
                        @Value("${cron.node-cron-job-responsible}") boolean cronJobResponsible,
                        @Value("${cron.nudge-app-mail-days-after-creation}") long nudgeAppMailDaysAfterCreation,
                        @Value("${feature.nudge_app_mail}") boolean nudgeAppMailFeature) {
        this.mailBox = mailBox;
        this.userRepository = userRepository;
        this.cronJobResponsible = cronJobResponsible;
        this.nudgeAppMailDaysAfterCreation = nudgeAppMailDaysAfterCreation;
        this.nudgeAppMailFeature = nudgeAppMailFeature;
    }

    @Scheduled(cron = "${cron.nudge-app-mail-expression}")
    @SuppressWarnings("unchecked")
    public void mailUsersWithoutApp() {
        if (!nudgeAppMailFeature || !cronJobResponsible) {
            return;
        }
        long start = System.currentTimeMillis();
        try {
            long createdBefore = (new Date().getTime() - (nudgeAppMailDaysAfterCreation * 24 * 60 * 60 * 1000)) / 1000;
            List<User> users = userRepository.findByNoEduIDApp(createdBefore);

            users.forEach(user -> {
                mailBox.sendNudgeAppMail(user);
                user.setNudgeAppMailSend(true);
                userRepository.save(user);
            });

            LOG.info(String.format("Mailed %s users to nudge the app in %s ms",
                    users.size(), System.currentTimeMillis() - start));
        } catch (Exception e) {
            LOG.error("Error in mailUsersWithInstitutionMail", e);
        }
    }

}
