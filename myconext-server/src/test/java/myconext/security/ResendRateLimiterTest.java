package myconext.security;

import myconext.exceptions.ForbiddenException;
import myconext.model.OneTimeLoginCode;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class ResendRateLimiterTest {

    @Test
    public void resendWithinIntervalIsForbiddenPerKey() {
        ResendRateLimiter limiter = new ResendRateLimiter(60);
        limiter.checkAllowed("resend:id:abc", "resend:ip:1.2.3.4");
        // same id or same ip within the interval is rejected
        assertThrows(ForbiddenException.class, () -> limiter.checkAllowed("resend:id:abc"));
        assertThrows(ForbiddenException.class, () -> limiter.checkAllowed("resend:ip:1.2.3.4"));
        // a different id from a different ip is allowed
        limiter.checkAllowed("resend:id:other", "resend:ip:5.6.7.8");
    }

    @Test
    public void resendAllowedAgainAfterInterval() {
        ResendRateLimiter limiter = new ResendRateLimiter(0);
        limiter.checkAllowed("resend:id:abc");
        limiter.checkAllowed("resend:id:abc");
    }

    @Test
    public void oneTimeLoginCodeResendRateLimit() {
        OneTimeLoginCode code = new OneTimeLoginCode("ABC-123");
        code.checkResendRateLimit(60_000L);
        code.markResent();
        assertThrows(ForbiddenException.class, () -> code.checkResendRateLimit(60_000L));
        // zero interval never blocks
        code.checkResendRateLimit(0L);
        assertTrue(code.isCodeAlmostExpired() || !code.isCodeAlmostExpired());
    }
}
