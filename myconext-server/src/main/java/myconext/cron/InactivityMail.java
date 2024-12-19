package myconext.cron;

import myconext.mail.MailBox;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static myconext.model.UserInactivity.*;

@Component
public class InactivityMail {

    private static final Log LOG = LogFactory.getLog(InactivityMail.class);

    private final MailBox mailBox;
    private final UserRepository userRepository;
    private final boolean mailInactivityMails;
    private final boolean cronJobResponsible;
    private final DateFormat dateFormatUS;
    private final DateFormat dateFormatNL;

    @Autowired
    public InactivityMail(MailBox mailBox,
                          UserRepository userRepository,
                          @Value("${cron.node-cron-job-responsible}") boolean cronJobResponsible,
                          @Value("${feature.mail_inactivity_mails}") boolean mailInactivityMails) {
        this.mailBox = mailBox;
        this.userRepository = userRepository;
        this.cronJobResponsible = cronJobResponsible;
        this.mailInactivityMails = mailInactivityMails;
        this.dateFormatUS = DateFormat.getDateInstance(DateFormat.LONG, Locale.of("us"));
        this.dateFormatNL = DateFormat.getDateInstance(DateFormat.LONG, Locale.of("nl"));
    }

    @Scheduled(cron = "${cron.inactivity-users-expression}")
    @SuppressWarnings("unchecked")
    public void mailInactiveUsers() {
        if (!mailInactivityMails || !cronJobResponsible) {
            return;
        }
        try {
            Stream.of(values()).forEach(this::doMailInactiveUsers);
            this.doDeleteInactiveUsers();
        } catch (Exception e) {
            //swallow exception as the scheduling stops then
            LOG.error("Error in mailInactiveUsers", e);
        }
    }

    private void doMailInactiveUsers(UserInactivity userInactivity) {
        long nowInMillis = System.currentTimeMillis();
        long oneDayInMillis = 24 * 60 * 60 * 1000L;
        long fiveYearsInMillis = 5 * 365 * oneDayInMillis;

        long lastLoginBefore = nowInMillis - (oneDayInMillis * userInactivity.getInactivityDays());
        List<User> users = userRepository.findByLastLoginBeforeAndUserInactivity(lastLoginBefore, userInactivity.getPreviousUserInactivity());

        Map<String, String> localeVariables = new HashMap<>();
        //This is the future date when the user will be deleted based on the inactivityDays of the userInactivity
        Date date = new Date(nowInMillis + (fiveYearsInMillis - (oneDayInMillis * userInactivity.getInactivityDays())));
        localeVariables.put("inactivity_period_en", userInactivity.getInactivityPeriodEn());
        localeVariables.put("inactivity_period_nl", userInactivity.getInactivityPeriodNl());
        localeVariables.put("deletion_period_en", userInactivity.getDeletionPeriodEn());
        localeVariables.put("deletion_period_nl", userInactivity.getDeletionPeriodNl());
        localeVariables.put("account_delete_date_en", dateFormatUS.format(date));
        localeVariables.put("account_delete_date_nl", dateFormatNL.format(date));
        users.forEach(user -> {
            mailBox.sendUserInactivityMail(user, localeVariables,
                    userInactivity.equals(YEAR_1_INTERVAL) || userInactivity.equals(YEAR_3_INTERVAL));
            user.setUserInactivity(userInactivity);
            userRepository.save(user);
        });
        LOG.info(String.format("Mailed %s users who has been inactive for %s period in for %s ms",
                users.size(), userInactivity, System.currentTimeMillis() - nowInMillis));
    }

    private void doDeleteInactiveUsers() {
        long nowInMillis = System.currentTimeMillis();
        long oneDayInMillis = 24 * 60 * 60 * 1000L;

        long lastLoginBefore = nowInMillis - (oneDayInMillis * 5L * 365);
        List<User> users = userRepository.findByLastLoginBeforeAndUserInactivity(lastLoginBefore, WEEK_1_BEFORE_5_YEARS);
        userRepository.deleteAll(users);
        LOG.info(String.format("Deleted %s users (%s) who has been inactive for 5 years in for %s ms",
                users.size(), users.stream().map(User::getEmail).collect(Collectors.joining(", ")),
                System.currentTimeMillis() - nowInMillis));
    }
}
