package myconext.security;

import myconext.exceptions.ForbiddenException;
import myconext.model.OneTimeLoginCode;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class ResendRateLimiterTest {

    @Test
    public void resendWithSameKeyWithinIntervalIsForbidden() {
        ResendRateLimiter limiter = new ResendRateLimiter(60, 10);
        limiter.checkAllowed("resend:id:abc", "1.2.3.4");
        assertThrows(ForbiddenException.class, () -> limiter.checkAllowed("resend:id:abc", "5.6.7.8"));
        // a different key is allowed
        limiter.checkAllowed("resend:id:other", "5.6.7.8");
    }

    @Test
    public void ipIsLimitedLeniently() {
        ResendRateLimiter limiter = new ResendRateLimiter(60, 3);
        // different users sharing one IP (NAT) are not blocked by the first resend
        limiter.checkAllowed("resend:id:1", "1.2.3.4");
        limiter.checkAllowed("resend:id:2", "1.2.3.4");
        limiter.checkAllowed("resend:id:3", "1.2.3.4");
        assertThrows(ForbiddenException.class, () -> limiter.checkAllowed("resend:id:4", "1.2.3.4"));
        // other IPs are not affected
        limiter.checkAllowed("resend:id:5", "5.6.7.8");
    }

    @Test
    public void rejectedRequestDoesNotConsumeKeyOrIpBudget() {
        ResendRateLimiter limiter = new ResendRateLimiter(60, 2);
        limiter.checkAllowed("resend:id:abc", "1.2.3.4");
        assertThrows(ForbiddenException.class, () -> limiter.checkAllowed("resend:id:abc", "1.2.3.4"));
        // the rejected request above did not use up the second IP slot
        limiter.checkAllowed("resend:id:other", "1.2.3.4");
    }

    @Test
    public void zeroIntervalDisablesLimiter() {
        ResendRateLimiter limiter = new ResendRateLimiter(0, 1);
        limiter.checkAllowed("resend:id:abc", "1.2.3.4");
        limiter.checkAllowed("resend:id:abc", "1.2.3.4");
    }

    @Test
    public void oneTimeLoginCodeResendRateLimit() {
        OneTimeLoginCode code = new OneTimeLoginCode("ABC-123");
        code.checkResendRateLimit(60_000L);
        code.markResent();
        assertThrows(ForbiddenException.class, () -> code.checkResendRateLimit(60_000L));
        // zero interval never blocks
        code.checkResendRateLimit(0L);
    }
}
