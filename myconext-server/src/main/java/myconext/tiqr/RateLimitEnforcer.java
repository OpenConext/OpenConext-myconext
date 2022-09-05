package myconext.tiqr;

import myconext.exceptions.TooManyRequestsException;
import myconext.model.User;
import myconext.repository.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

import static myconext.tiqr.SURFSecureID.*;

public class RateLimitEnforcer {

    private final UserRepository userRepository;
    private final TiqrConfiguration tiqrConfiguration;

    public RateLimitEnforcer(UserRepository userRepository, TiqrConfiguration tiqrConfiguration) {
        this.userRepository = userRepository;
        this.tiqrConfiguration = tiqrConfiguration;
    }

    public void checkRateLimit(User user) {
        Map<String, Object> surfSecureId = user.getSurfSecureId();
        int rateLimit = (int) surfSecureId.merge(RATE_LIMIT, 1, (i, j) -> Integer.sum((int) i, (int) j));
        if (rateLimit >= tiqrConfiguration.getRateLimitThreshold()) {
            Long rateLimitUpdated = (Long) surfSecureId.get(RATE_LIMIT_UPDATED);
            boolean resetRateLimit = (System.currentTimeMillis() - rateLimitUpdated) > (tiqrConfiguration.getRateLimitResetMinutes() * 1000L * 60);
            if (resetRateLimit) {
                surfSecureId.put(RATE_LIMIT, 0);
            } else {
                throw new TooManyRequestsException();
            }
        }
        surfSecureId.put(RATE_LIMIT_UPDATED, System.currentTimeMillis());
        userRepository.save(user);
    }

    public void suspendUserAfterTiqrFailure(User user) {
        Map<String, Object> surfSecureId = user.getSurfSecureId();
        int attempts = (int) surfSecureId.merge(SUSPENDED_ATTEMPTS, 1, (i, j) -> (int) i + 1);
        surfSecureId.put(SUSPENDED_UNTIL, Instant.now().plus((int) Math.pow(attempts - 1, 2), ChronoUnit.MINUTES));
        userRepository.save(user);
    }

    public void unsuspendUserAfterTiqrSuccess(User user) {
        Map<String, Object> surfSecureId = user.getSurfSecureId();
        if (surfSecureId.containsKey(SUSPENDED_UNTIL)) {
            surfSecureId.remove(SUSPENDED_ATTEMPTS);
            surfSecureId.remove(SUSPENDED_UNTIL);
            userRepository.save(user);
        }
    }

    public boolean isUserAllowedTiqrVerification(User user) {
        Map<String, Object> surfSecureId = user.getSurfSecureId();
        if (surfSecureId.containsKey(SUSPENDED_UNTIL)) {
            Object suspendedUntil = surfSecureId.get(SUSPENDED_UNTIL);
            Instant suspendedUntilInstant = suspendedUntil instanceof Date ? ((Date) suspendedUntil).toInstant() : (Instant) suspendedUntil;
            return Instant.now().isAfter(suspendedUntilInstant);
        }
        return true;
    }
}
