package myconext.tiqr;

import myconext.exceptions.TooManyRequestsException;
import myconext.model.User;
import myconext.repository.UserRepository;

import java.util.Map;

public class RateLimitEnforcer {

    private final UserRepository userRepository;
    private final TiqrConfiguration tiqrConfiguration;

    public RateLimitEnforcer(UserRepository userRepository, TiqrConfiguration tiqrConfiguration) {
        this.userRepository = userRepository;
        this.tiqrConfiguration = tiqrConfiguration;
    }

    public void checkRateLimit(User user) {
        Map<String, Object> surfSecureId = user.getSurfSecureId();
        int rateLimit = (int) surfSecureId.merge(SURFSecureID.RATE_LIMIT, 1, (i, j) -> Integer.sum((int) i, (int) j));
        userRepository.save(user);
        if (rateLimit >= tiqrConfiguration.getRateLimitThreshold()) {
            throw new TooManyRequestsException();
        }

    }
}
