package myconext.cron;

import myconext.mail.MailBox;
import myconext.manage.Manage;
import myconext.model.User;
import myconext.model.UserInactivity;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InactivityMail {

    private static final Log LOG = LogFactory.getLog(InactivityMail.class);

    private final MailBox mailBox;
    private final UserRepository userRepository;
    private final boolean mailInactivityMails;
    private final boolean cronJobResponsible;

    @Autowired
    public InactivityMail(Manage manage,
                                MailBox mailBox,
                                UserRepository userRepository,
                                @Value("${cron.node-cron-job-responsible}") boolean cronJobResponsible,
                                @Value("${feature.mail_inactivity_mails}") boolean mailInactivityMails) {
        this.mailBox = mailBox;
        this.userRepository = userRepository;
        this.cronJobResponsible = cronJobResponsible;
        this.mailInactivityMails = mailInactivityMails;
    }

    @Scheduled(cron = "${cron.inactivity-users-expression}")
    @SuppressWarnings("unchecked")
    public void mailInactiveUsers() {
        if (!mailInactivityMails || !cronJobResponsible) {
            return;
        }
        long start = System.currentTimeMillis();
        try {

            List<User> users = userRepository.findByLastLoginBeforeAndLastLoginAfterAndUserInactivity(0L, 0L, null);

            LOG.info(String.format("Mailed %s users who has been inactive for %s in for %s ms",
                    users.size(), UserInactivity.YEAR_1_INTERVAL,  System.currentTimeMillis() - start));
        } catch (Exception e) {
            LOG.error("Error in mailInactiveUsers", e);
        }
    }


}
