package myconext.cron;

import com.mongodb.client.MongoClient;
import myconext.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tiqr.org.model.Authentication;
import tiqr.org.model.Enrollment;
import tiqr.org.model.Registration;
import tiqr.org.model.RegistrationStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TiqrCleanerTest extends AbstractIntegrationTest {

    @Autowired
    protected MongoClient mongoClient;

    @Value("${mongodb_db}")
    protected String databaseName;

    @Test
    void clean() {
        TiqrCleaner tiqrCleaner = getTiqrCleaner();

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
    void cleanNoLockAcquired() {
        TiqrCleaner tiqrCleaner = getTiqrCleaner();
        String nodeId = tiqrCleaner.generateNodeId();
        try {
            tiqrCleaner.tryGetLock(TiqrCleaner.LOCK_NAME, nodeId);

            Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);

            registration(oneDayAgo, RegistrationStatus.INITIALIZED);

            tiqrCleaner.clean();

            assertEquals(1, registrationRepository.count());
        } finally {
            tiqrCleaner.releaseLock(TiqrCleaner.LOCK_NAME, nodeId);
        }
    }

    private void registration(Instant oneDayAgo, RegistrationStatus status) {
        Registration registration = new Registration();
        registration.setUpdated(oneDayAgo);
        registration.setStatus(status);
        registration.setUserId(UUID.randomUUID().toString());
        registrationRepository.saveAll(Arrays.asList(registration));
    }


    private TiqrCleaner getTiqrCleaner() {
        return new TiqrCleaner(
                registrationRepository,
                authenticationRepository,
                enrollmentRepository,
                userRepository,
                mongoClient,
                true,
                databaseName);
    }
}