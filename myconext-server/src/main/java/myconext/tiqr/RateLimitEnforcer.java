package myconext.tiqr;

import myconext.exceptions.TooManyRequestsException;
import myconext.model.User;
import myconext.repository.UserRepository;

import java.time.Clock;
import java.util.Map;

import static myconext.tiqr.SURFSecureID.RATE_LIMIT;
import static myconext.tiqr.SURFSecureID.RATE_LIMIT_UPDATED;

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
}
