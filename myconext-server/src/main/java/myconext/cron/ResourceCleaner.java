package myconext.cron;


import com.mongodb.client.MongoClient;
import myconext.model.*;
import myconext.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static myconext.cron.InactivityMail.ONE_DAY_IN_MILLIS;

@Component
public class ResourceCleaner extends AbstractNodeLeader {

    public static final String LOCK_NAME =  "resource_cleaner_lock_name";

    private static final Log LOG = LogFactory.getLog(ResourceCleaner.class);

    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;
    private final PasswordResetHashRepository passwordResetHashRepository;
    private final ChangeEmailHashRepository changeEmailHashRepository;
    private final EmailsSendRepository emailsSendRepository;
    private final RequestInstitutionEduIDRepository requestInstitutionEduIDRepository;
    private final MobileLinkAccountRequestRepository mobileLinkAccountRequestRepository;

    @Autowired
    public ResourceCleaner(AuthenticationRequestRepository authenticationRequestRepository,
                           UserRepository userRepository,
                           PasswordResetHashRepository passwordResetHashRepository,
                           ChangeEmailHashRepository changeEmailHashRepository,
                           EmailsSendRepository emailsSendRepository,
                           RequestInstitutionEduIDRepository requestInstitutionEduIDRepository,
                           MobileLinkAccountRequestRepository mobileLinkAccountRequestRepository,
                           MongoClient mongoClient,
                           @Value("${mongodb_db}") String databaseName) {
        super(LOCK_NAME, mongoClient, databaseName);

        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.passwordResetHashRepository = passwordResetHashRepository;
        this.changeEmailHashRepository = changeEmailHashRepository;
        this.emailsSendRepository = emailsSendRepository;
        this.requestInstitutionEduIDRepository = requestInstitutionEduIDRepository;
        this.mobileLinkAccountRequestRepository = mobileLinkAccountRequestRepository;
    }

    @Scheduled(cron = "${cron.token-cleaner-expression}")
    public void clean() {
        super.perform("ResourceCleaner#clean", () -> doClean());
    }

    private void doClean() {
        Date now = new Date();
        Instant nowInstant = now.toInstant();

        info(SamlAuthenticationRequest.class, authenticationRequestRepository.deleteByExpiresInBeforeAndRememberMe(now, false));
        info(PasswordResetHash.class, passwordResetHashRepository.deleteByExpiresInBefore(now));
        info(ChangeEmailHash.class, changeEmailHashRepository.deleteByExpiresInBefore(now));
        info(RequestInstitutionEduID.class, requestInstitutionEduIDRepository.deleteByExpiresInBefore(now));
        info(MobileLinkAccountRequest.class, mobileLinkAccountRequestRepository.deleteByExpiresInBefore(now));

        Date seconds16Ago = Date.from(nowInstant.minus(16, ChronoUnit.SECONDS));
        info(EmailsSend.class, emailsSendRepository.deleteBySendAtBefore(seconds16Ago));

        List<User> users = userRepository.findByLinkedAccounts_ExpiresAtBefore(now);
        users.forEach(user -> {
            List<LinkedAccount> linkedAccounts = user.getLinkedAccounts().stream()
                    .filter(linkedAccount -> linkedAccount.getExpiresAt().toInstant().isAfter(nowInstant))
                    .collect(Collectors.toList());
            user.setLinkedAccounts(linkedAccounts);
            LOG.info(String.format("Removed expired linked account for user %s", user.getEmail()));
            userRepository.save(user);
        });

        long twoWeeksAgo = System.currentTimeMillis() - (ONE_DAY_IN_MILLIS * 14);
        List<User> controlCodeExpiredUsers = userRepository.findByControlCode_CreatedAtLessThan(twoWeeksAgo);
        controlCodeExpiredUsers.forEach(user -> {
            user.setControlCode(null);
            LOG.info(String.format("Removed expired control code for user %s", user.getEmail()));
            userRepository.save(user);
        });

        long dayAgo = nowInstant.minus(1, ChronoUnit.DAYS).toEpochMilli() / 1000L;
        List<User> newUsersExpired = userRepository.findByNewUserTrueAndCreatedLessThan(dayAgo);
        if (newUsersExpired.isEmpty()) {
            LOG.info("No users found that have not finished registration last 24 hours");
        } else {
            LOG.info(String.format(
                    "Removing new users that have not finished registration last 24 hours %s",
                    newUsersExpired.stream().map(User::getEmail).collect(Collectors.joining(", "))));
            userRepository.deleteAll(newUsersExpired);
        }
    }

    private void info(Class clazz, long count) {
        LOG.info(String.format("Deleted %s instances of %s in cleanup", count, clazz));
    }
}
