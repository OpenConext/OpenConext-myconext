package myconext.cron;


import myconext.model.*;
import myconext.repository.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ResourceCleaner {

    private static final Log LOG = LogFactory.getLog(ResourceCleaner.class);

    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;
    private final PasswordResetHashRepository passwordResetHashRepository;
    private final ChangeEmailHashRepository changeEmailHashRepository;
    private final boolean cronJobResponsible;
    private final EmailsSendRepository emailsSendRepository;

    @Autowired
    public ResourceCleaner(AuthenticationRequestRepository authenticationRequestRepository,
                           UserRepository userRepository,
                           PasswordResetHashRepository passwordResetHashRepository,
                           ChangeEmailHashRepository changeEmailHashRepository,
                           EmailsSendRepository emailsSendRepository,
                           @Value("${cron.node-cron-job-responsible}") boolean cronJobResponsible) {
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.passwordResetHashRepository = passwordResetHashRepository;
        this.changeEmailHashRepository = changeEmailHashRepository;
        this.emailsSendRepository = emailsSendRepository;
        this.cronJobResponsible = cronJobResponsible;
    }

    @Scheduled(cron = "${cron.token-cleaner-expression}")
    public void clean() {
        if (!cronJobResponsible) {
            return;
        }
        Date now = new Date();
        info(SamlAuthenticationRequest.class, authenticationRequestRepository.deleteByExpiresInBeforeAndRememberMe(now, false));
        info(PasswordResetHash.class, passwordResetHashRepository.deleteByExpiresInBefore(now));
        info(ChangeEmailHash.class, changeEmailHashRepository.deleteByExpiresInBefore(now));

        Date seconds16Ago = Date.from(now.toInstant().minus(16, ChronoUnit.SECONDS));
        info(EmailsSend.class, emailsSendRepository.deleteBySendAtBefore(seconds16Ago));

        List<User> users = userRepository.findByLinkedAccounts_ExpiresAtBefore(now);
        users.forEach(user -> {
            List<LinkedAccount> linkedAccounts = user.getLinkedAccounts().stream()
                    .filter(linkedAccount -> linkedAccount.getExpiresAt().toInstant().isAfter(now.toInstant()))
                    .collect(Collectors.toList());
            user.setLinkedAccounts(linkedAccounts);
            LOG.info(String.format("Removed expired linked account for user %s", user.getEmail()));
            userRepository.save(user);
        });

        long dayAgo = now.toInstant().minus(1, ChronoUnit.DAYS).toEpochMilli() / 1000L;
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
