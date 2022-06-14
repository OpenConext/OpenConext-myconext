package myconext.tiqr;

import myconext.exceptions.TooManyRequestsException;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.junit.Test;
import org.mockito.Mockito;

import static myconext.tiqr.SURFSecureID.RATE_LIMIT;
import static myconext.tiqr.SURFSecureID.RATE_LIMIT_UPDATED;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RateLimitEnforcerTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private final TiqrConfiguration tiqrConfiguration = new TiqrConfiguration();
    private final RateLimitEnforcer rateLimitEnforcer = new RateLimitEnforcer(userRepository, tiqrConfiguration);

    @Test
    public void rateLimit() {
        User user = new User();
        rateLimitEnforcer.checkRateLimit(user);

        assertEquals(1, user.getSurfSecureId().get(RATE_LIMIT));

        rateLimitEnforcer.checkRateLimit(user);

        assertEquals(2, user.getSurfSecureId().get(RATE_LIMIT));
    }

    @Test(expected = TooManyRequestsException.class)
    public void rateLimitExceeded() {
        User user = new User();
        user.getSurfSecureId().put(RATE_LIMIT, 5);
        user.getSurfSecureId().put(RATE_LIMIT_UPDATED, System.currentTimeMillis());
        rateLimitEnforcer.checkRateLimit(user);
    }

    @Test
    public void rateLimitReset() {
        User user = new User();
        user.getSurfSecureId().put(RATE_LIMIT, 5);
        user.getSurfSecureId().put(RATE_LIMIT_UPDATED, System.currentTimeMillis() - 1000 * 60 * 45);
        rateLimitEnforcer.checkRateLimit(user);
        assertEquals(0, user.getSurfSecureId().get(RATE_LIMIT));
    }

}