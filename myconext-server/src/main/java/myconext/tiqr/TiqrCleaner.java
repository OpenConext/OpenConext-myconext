package myconext.tiqr;


import myconext.repository.AuthenticationRepository;
import myconext.repository.EnrollmentRepository;
import myconext.repository.RegistrationRepository;
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

@Component
public class TiqrCleaner {

    private static final Log LOG = LogFactory.getLog(TiqrCleaner.class);

    private final RegistrationRepository registrationRepository;
    private final AuthenticationRepository authenticationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final boolean cronJobResponsible;

    @Autowired
    public TiqrCleaner(RegistrationRepository registrationRepository,
                       AuthenticationRepository authenticationRepository,
                       EnrollmentRepository enrollmentRepository,
                       @Value("${cron.node-cron-job-responsible}") boolean cronJobResponsible) {
        this.registrationRepository = registrationRepository;
        this.authenticationRepository = authenticationRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.cronJobResponsible = cronJobResponsible;
    }

    @Scheduled(cron = "${cron.token-cleaner-expression}")
    public void clean() {
        if (!cronJobResponsible) {
            return;
        }
        Instant hourAgo = Instant.now().minus(1, ChronoUnit.HOURS);

        info(Authentication.class, authenticationRepository.deleteByUpdatedBefore(hourAgo));
        info(Registration.class, registrationRepository.deleteByUpdatedBeforeAndStatus(hourAgo, RegistrationStatus.INITIALIZED));
        info(Enrollment.class, enrollmentRepository.deleteByUpdatedBefore(hourAgo));
    }

    private void info(Class clazz, long count) {
        LOG.info(String.format("Deleted %s instances of %s in cleanup", count, clazz));
    }
}
