package myconext.tiqr;

import myconext.exceptions.TooManyRequestsException;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Map;

import static myconext.tiqr.SURFSecureID.*;
import static org.junit.jupiter.api.Assertions.*;

public class RateLimitEnforcerTest {

    private final UserRepository userRepository = Mockito.mock(UserRepository.class);
    private TiqrConfiguration tiqrConfiguration;
    private RateLimitEnforcer rateLimitEnforcer;

    @Before
    public void before() {
        this.tiqrConfiguration = new TiqrConfiguration();
        this.rateLimitEnforcer = new RateLimitEnforcer(userRepository, tiqrConfiguration);

    }

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

    @Test
    public void suspendUserAfterTiqrFailure() {
        User user = new User();
        Map<String, Object> surfSecureId = user.getSurfSecureId();

        rateLimitEnforcer.suspendUserAfterTiqrFailure(user);
        assertEquals(1, (int) surfSecureId.get(SUSPENDED_ATTEMPTS));

        rateLimitEnforcer.suspendUserAfterTiqrFailure(user);
        assertEquals(2, (int) surfSecureId.get(SUSPENDED_ATTEMPTS));

        Instant suspendedUntil = (Instant) surfSecureId.get(SUSPENDED_UNTIL);
        assertTrue(suspendedUntil.isAfter(Instant.now()));

        assertFalse(rateLimitEnforcer.isUserAllowedTiqrVerification(user));

        rateLimitEnforcer.unsuspendUserAfterTiqrSuccess(user);
        assertTrue(rateLimitEnforcer.isUserAllowedTiqrVerification(user));
    }

    @Test
    public void rateLimitSMS() {
        User user = new User();

        initializeSmsRateLimiterWithShortSMSDelay();
        rateLimitEnforcer.checkSendSMSRateLimit(user);

        assertEquals(1, user.getSurfSecureId().get(SMS_RATE_LIMIT));

        rateLimitEnforcer.checkSendSMSRateLimit(user);

        assertEquals(2, user.getSurfSecureId().get(SMS_RATE_LIMIT));
    }

    @Test(expected = TooManyRequestsException.class)
    public void smsRateLimitExceeded() {
        this.initializeSmsRateLimiterWithShortSMSDelay();

        User user = new User();
        user.getSurfSecureId().put(SMS_RATE_LIMIT, 5);
        user.getSurfSecureId().put(SMS_RATE_LIMIT_UPDATED, System.currentTimeMillis());
        rateLimitEnforcer.checkSendSMSRateLimit(user);
    }

    @Test
    public void smsRateLimitReset() {
        User user = new User();

        this.initializeSmsRateLimiterWithShortSMSDelay();

        user.getSurfSecureId().put(SMS_RATE_LIMIT, 5);
        user.getSurfSecureId().put(SMS_RATE_LIMIT_UPDATED, System.currentTimeMillis() - 1000 * 60 * 60 * 25);
        rateLimitEnforcer.checkSendSMSRateLimit(user);
        assertEquals(0, user.getSurfSecureId().get(SMS_RATE_LIMIT));
    }

    private void initializeSmsRateLimiterWithShortSMSDelay() {
        this.tiqrConfiguration = new TiqrConfiguration();
        tiqrConfiguration.setSmsSendingDelayInMillis(0);

        this.rateLimitEnforcer = new RateLimitEnforcer(userRepository, tiqrConfiguration);
    }


}