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

    public static final long ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L;

    private static final Log LOG = LogFactory.getLog(InactivityMail.class);

    private final MailBox mailBox;
    private final UserRepository userRepository;
    private final boolean mailInactivityMails;
    private final boolean cronJobResponsible;
    private final DateFormat dateFormatUS;
    private final DateFormat dateFormatNL;
    private final boolean dryRunEmail;

    @Autowired
    public InactivityMail(MailBox mailBox,
                          UserRepository userRepository,
                          @Value("${cron.node-cron-job-responsible}") boolean cronJobResponsible,
                          @Value("${feature.mail_inactivity_mails}") boolean mailInactivityMails,
                          @Value("${cron.dry-run-email}") boolean dryRunEmail) {
        this.mailBox = mailBox;
        this.userRepository = userRepository;
        this.cronJobResponsible = cronJobResponsible;
        this.mailInactivityMails = mailInactivityMails;
        this.dryRunEmail = dryRunEmail;
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
            //We need to process the users in reverse order to prevent multiple emails to one user in the initial run
            List<UserInactivity> userInactivities = Stream.of(values()).toList().reversed();
            userInactivities.forEach(this::doMailInactiveUsers);
            this.doDeleteInactiveUsers();
        } catch (Exception e) {
            //swallow exception as the scheduling stops then
            LOG.error("Error in mailInactiveUsers", e);
        }
    }

    private void doMailInactiveUsers(UserInactivity userInactivity) {
        long nowInMillis = System.currentTimeMillis();
        long fiveYearsInMillis = 5 * 365 * ONE_DAY_IN_MILLIS;

        long lastLoginBefore = nowInMillis - (ONE_DAY_IN_MILLIS * userInactivity.getInactivityDays());
        List<User> users = userRepository.findByLastLoginBeforeAndUserInactivityIn(lastLoginBefore,
                this.userInactivitiesWithNullElement(userInactivity.getPreviousUserInactivity()));

        Map<String, String> localeVariables = new HashMap<>();
        //This is the future date when the user will be deleted based on the inactivityDays of the userInactivity
        Date date = new Date(nowInMillis + (fiveYearsInMillis - (ONE_DAY_IN_MILLIS * userInactivity.getInactivityDays())));
        localeVariables.put("inactivity_period_en", userInactivity.getInactivityPeriodEn());
        localeVariables.put("inactivity_period_nl", userInactivity.getInactivityPeriodNl());
        localeVariables.put("deletion_period_en", userInactivity.getDeletionPeriodEn());
        localeVariables.put("deletion_period_nl", userInactivity.getDeletionPeriodNl());
        localeVariables.put("account_delete_date_en", dateFormatUS.format(date));
        localeVariables.put("account_delete_date_nl", dateFormatNL.format(date));

        if (!dryRunEmail) {
            users.forEach(user -> {
                mailBox.sendUserInactivityMail(user, localeVariables,
                        userInactivity.equals(YEAR_1_INTERVAL) || userInactivity.equals(YEAR_3_INTERVAL));
                user.setUserInactivity(userInactivity);
                //Ensure users who receive their last warning are not deleted the next run, but after one week
                if (userInactivity.equals(WEEK_1_BEFORE_5_YEARS)) {
                    user.setLastLogin(nowInMillis - (WEEK_1_BEFORE_5_YEARS.getInactivityDays() * ONE_DAY_IN_MILLIS));
                }
                userRepository.save(user);
            });
        }
        LOG.info(String.format("Mailed %s users who has been inactive for %s period in for %s ms, dry run: %s",
                users.size(), userInactivity, System.currentTimeMillis() - nowInMillis, dryRunEmail));
    }

    private void doDeleteInactiveUsers() {
        long nowInMillis = System.currentTimeMillis();
        long oneDayInMillis = 24 * 60 * 60 * 1000L;

        long lastLoginBefore = nowInMillis - (oneDayInMillis * 5L * 365);
        List<User> users = userRepository.findByLastLoginBeforeAndUserInactivityIn(lastLoginBefore, List.of(WEEK_1_BEFORE_5_YEARS));
        if (!dryRunEmail) {
            userRepository.deleteAll(users);
        }
        LOG.info(String.format("Deleted %s users (%s) who has been inactive for 5 years in for %s ms, dry-run: %s",
                users.size(), users.stream().map(User::getEmail).collect(Collectors.joining(", ")),
                System.currentTimeMillis() - nowInMillis,
                dryRunEmail));
    }

    private List<UserInactivity> userInactivitiesWithNullElement(UserInactivity userInactivity) {
        //Can't use List.of as this does not permit null values
        List<UserInactivity> userInactivities = new ArrayList<>();
        userInactivities.add(userInactivity);
        userInactivities.add(null);
        return userInactivities;
    }
}
