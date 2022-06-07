package myconext.tiqr;

import myconext.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import tiqr.org.model.Authentication;
import tiqr.org.model.Enrollment;
import tiqr.org.model.Registration;
import tiqr.org.model.RegistrationStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TiqrCleanerTest extends AbstractIntegrationTest {

    @Test
    void clean() {
        TiqrCleaner tiqrCleaner = getTiqrCleaner(true);

        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);

        registration(oneDayAgo, RegistrationStatus.INITIALIZED);
        registration(oneDayAgo, RegistrationStatus.FINALIZED);
        assertEquals(2, registrationRepository.count());

        Authentication authentication = new Authentication();
        authentication.setUpdated(oneDayAgo);
        authenticationRepository.saveAll(Arrays.asList(authentication));
        assertEquals(1, authenticationRepository.count());

        Enrollment enrollment = new Enrollment();
        enrollment.setUpdated(oneDayAgo);
        enrollmentRepository.saveAll(Arrays.asList(enrollment));
        assertEquals(1, enrollmentRepository.count());

        tiqrCleaner.clean();

        assertEquals(1, registrationRepository.count());
        assertEquals(RegistrationStatus.FINALIZED, registrationRepository.findAll().get(0).getStatus());
        assertEquals(0, authenticationRepository.count());
        assertEquals(0, enrollmentRepository.count());

    }

    @Test
    void cleanNoCronJobResponsible() {
        TiqrCleaner tiqrCleaner = getTiqrCleaner(false);

        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);

        registration(oneDayAgo, RegistrationStatus.INITIALIZED);

        tiqrCleaner.clean();

        assertEquals(1, registrationRepository.count());
    }

    private void registration(Instant oneDayAgo, RegistrationStatus status) {
        Registration registration = new Registration();
        registration.setUpdated(oneDayAgo);
        registration.setStatus(status);
        registrationRepository.saveAll(Arrays.asList(registration));
    }


    private TiqrCleaner getTiqrCleaner(boolean cronJobResponsible) {
        return new TiqrCleaner(registrationRepository, authenticationRepository, enrollmentRepository, cronJobResponsible);
    }
}