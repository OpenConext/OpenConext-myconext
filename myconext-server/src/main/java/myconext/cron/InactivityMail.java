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

import java.text.DateFormat;
import java.util.*;

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
        long nowInMillis = System.currentTimeMillis();
        long oneDayInMillis = 24 * 60 * 60 * 1000;
        DateFormat dateFormatNL = DateFormat.getDateInstance(DateFormat.LONG, Locale.of("nl"));
        DateFormat dateFormatUS = DateFormat.getDateInstance(DateFormat.LONG, Locale.of("us"));
        try {
            //For all UserInactivity we check the last activity date and ensure they only receive one mail about this
            long lastLoginBefore = nowInMillis - (oneDayInMillis * UserInactivity.YEAR_1_INTERVAL.getInactivityDays());
            List<User> users = userRepository.findByLastLoginBeforeAndUserInactivity(UserInactivity.YEAR_1_INTERVAL.getInactivityDays(), null);
            //We need the following localized variables
            //inactivity_period_en, deletion_period_en, account_delete_date_en, inactivity_period_nl, deletion_period_nl, account_delete_date_nl
            Map<String, String> localeVariables = new HashMap();
            Date date = new Date(lastLoginBefore);
            localeVariables.put("inactivity_period_en", "1 year");
            localeVariables.put("deletion_period_en", "4 years");
            localeVariables.put("account_delete_date_en", dateFormatUS.format(date));
            localeVariables.put("inactivity_period_nl", "1 jaar");
            localeVariables.put("deletion_period_nl", "4 jaren");
            localeVariables.put("account_delete_date_nl", dateFormatNL.format(date));
            users.forEach(user -> {
                mailBox.sendUserInactivityMail(user, true);
            });


            LOG.info(String.format("Mailed %s users who has been inactive for %s in for %s ms",
                    users.size(), UserInactivity.YEAR_1_INTERVAL,  System.currentTimeMillis() - nowInMillis));
        } catch (Exception e) {
            LOG.error("Error in mailInactiveUsers", e);
        }
    }


}
