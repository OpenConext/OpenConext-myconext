package myconext.repository;

import myconext.AbstractIntegrationTest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static myconext.tiqr.SURFSecureID.SMS_RATE_LIMIT;
import static myconext.tiqr.SURFSecureID.SMS_RATE_LIMIT_UPDATED;
import static org.junit.Assert.*;

public class SurfSecureIdRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private SurfSecureIdRepository surfSecureIdRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void incrementBelowThreshold() {
        User user = newUser("sms-limit-below@example.com");
        assertEquals(Integer.valueOf(1), surfSecureIdRepository.incrementSmsRateLimitIfAllowed(user.getId(), 5, 1440));
        assertEquals(Integer.valueOf(2), surfSecureIdRepository.incrementSmsRateLimitIfAllowed(user.getId(), 5, 1440));
    }

    @Test
    public void rejectsAtThresholdWithoutChangingCounter() {
        User user = newUser("sms-limit-threshold@example.com");
        for (int i = 0; i < 4; i++) {
            assertNotNull(surfSecureIdRepository.incrementSmsRateLimitIfAllowed(user.getId(), 5, 1440));
        }
        assertNull(surfSecureIdRepository.incrementSmsRateLimitIfAllowed(user.getId(), 5, 1440));

        User reloaded = userRepository.findById(user.getId()).get();
        assertEquals(4, reloaded.getSurfSecureId().get(SMS_RATE_LIMIT));
    }

    @Test
    public void resetsAfterResetWindowPassed() {
        User user = newUser("sms-limit-reset@example.com");
        user.getSurfSecureId().put(SMS_RATE_LIMIT, 5);
        user.getSurfSecureId().put(SMS_RATE_LIMIT_UPDATED, System.currentTimeMillis() - 1000L * 60 * 60 * 25);
        userRepository.save(user);

        assertEquals(Integer.valueOf(0), surfSecureIdRepository.incrementSmsRateLimitIfAllowed(user.getId(), 5, 1440));
    }

    @Test
    public void concurrentCallsNeverExceedThreshold() throws Exception {
        User user = newUser("sms-limit-concurrent@example.com");
        int threshold = 5;
        int attempts = 50;
        ExecutorService executor = Executors.newFixedThreadPool(16);
        List<Future<Integer>> futures = IntStream.range(0, attempts)
                .mapToObj(i -> executor.submit(() -> surfSecureIdRepository.incrementSmsRateLimitIfAllowed(user.getId(), threshold, 1440)))
                .collect(Collectors.toList());
        long allowed = futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(Objects::nonNull)
                .count();
        executor.shutdown();

        assertEquals(threshold - 1, allowed);
        User reloaded = userRepository.findById(user.getId()).get();
        assertEquals(threshold - 1, reloaded.getSurfSecureId().get(SMS_RATE_LIMIT));
    }

    private User newUser(String email) {
        User user = new User();
        user.setEmail(email);
        return userRepository.save(user);
    }
}
