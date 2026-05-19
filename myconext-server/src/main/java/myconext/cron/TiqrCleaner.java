package myconext.cron;


import com.mongodb.client.MongoClient;
import myconext.repository.AuthenticationRepository;
import myconext.repository.EnrollmentRepository;
import myconext.repository.RegistrationRepository;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tiqr.org.model.Authentication;
import tiqr.org.model.Enrollment;
import tiqr.org.model.Registration;
import tiqr.org.model.RegistrationStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class TiqrCleaner extends AbstractNodeLeader {

    public static final String LOCK_NAME = "tiqr_cleaner_usage_lock_name";

    private static final Log LOG = LogFactory.getLog(TiqrCleaner.class);

    private final RegistrationRepository registrationRepository;
    private final AuthenticationRepository authenticationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Autowired
    public TiqrCleaner(RegistrationRepository registrationRepository,
                       AuthenticationRepository authenticationRepository,
                       EnrollmentRepository enrollmentRepository,
                       UserRepository userRepository,
                       MongoClient mongoClient,
                       @Value("${cron.node-cron-job-responsible}") boolean cronJobResponsible,
                       @Value("${mongodb_db}") String databaseName) {
        super(LOCK_NAME, mongoClient, databaseName, cronJobResponsible);

        this.registrationRepository = registrationRepository;
        this.authenticationRepository = authenticationRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "${cron.token-cleaner-expression}")
    public void clean() {
        super.perform("TiqrCleaner#clean", this::doClean);
    }

    private void doClean() {
        Instant hourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        info(Authentication.class, authenticationRepository.deleteByUpdatedBefore(hourAgo));

        List<Registration> registrations = registrationRepository.findByUpdatedBeforeAndStatus(hourAgo, RegistrationStatus.INITIALIZED);
        long deletedCount = 0;
        for (Registration registration : registrations) {
            userRepository.findById(registration.getUserId()).ifPresent(user -> {
                user.getSurfSecureId().clear();
                userRepository.save(user);
                LOG.info(String.format("Clean surfSecureId settings for user %s, because of stale and initialized registration", user.getEmail()));
            });
            registrationRepository.delete(registration);
            deletedCount++;
        }
        info(Registration.class, deletedCount);

        info(Enrollment.class, enrollmentRepository.deleteByUpdatedBefore(hourAgo));
    }

    private void info(Class clazz, long count) {
        LOG.info(String.format("Deleted Tiqr %s instances of %s in cleanup", count, clazz));
    }
}
