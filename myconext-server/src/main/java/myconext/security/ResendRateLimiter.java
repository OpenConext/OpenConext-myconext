package myconext.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import myconext.exceptions.ForbiddenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * In-memory rate limiter for resend-code endpoints. Keys are namespaced (e.g. "resend:id:..." or "resend:ip:..."),
 * so the same component can limit per authentication-request id and per source IP. Per-target-user limiting is
 * persisted on the OneTimeLoginCode itself (survives restarts), mirroring the brute-force delay on verification.
 */
@Component
public class ResendRateLimiter {

    private final long minIntervalMillis;
    private final Cache<String, Long> lastAllowedAt = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();

    public ResendRateLimiter(@Value("${resend_rate_limit_seconds:60}") long minIntervalSeconds) {
        this.minIntervalMillis = minIntervalSeconds * 1000L;
    }

    public long getMinIntervalMillis() {
        return minIntervalMillis;
    }

    public void checkAllowed(String... keys) {
        long now = System.currentTimeMillis();
        for (String key : keys) {
            Long last = key == null ? null : lastAllowedAt.getIfPresent(key);
            if (last != null && now < last + minIntervalMillis) {
                throw new ForbiddenException("Resend forbidden due to rate limit");
            }
        }
        for (String key : keys) {
            if (key != null) {
                lastAllowedAt.put(key, now);
            }
        }
    }
}
