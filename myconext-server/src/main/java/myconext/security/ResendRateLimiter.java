package myconext.security;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import myconext.exceptions.ForbiddenException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * In-memory rate limiter for resend-code endpoints.
 * <ul>
 *     <li>A key (e.g. the authentication-request id) may be used at most once per interval.</li>
 *     <li>A source IP may trigger at most {@code maxPerIp} resends per interval. This is deliberately more
 *     lenient than the key limit, because many users can share one IP (NAT, institutional proxies).</li>
 * </ul>
 * Per-target-user limiting is persisted on the OneTimeLoginCode itself (survives restarts), mirroring the
 * brute-force delay on verification. An interval of zero disables the limiter.
 * <p>
 * The interval must not be longer than the GUI's resend delay (email_spam_threshold_seconds), otherwise a
 * legitimate user clicking resend as soon as the GUI allows it is rejected.
 */
@Component
public class ResendRateLimiter {

    private static final long MAX_ENTRIES = 100_000L;

    private static class IpWindow {
        private long windowStart;
        private int count;
    }

    private final long minIntervalMillis;
    private final int maxPerIp;
    private final Cache<String, Long> lastAllowedAt = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(MAX_ENTRIES)
            .build();
    private final Cache<String, IpWindow> ipWindows = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(MAX_ENTRIES)
            .build();

    public ResendRateLimiter(@Value("${resend_rate_limit_seconds:${email_spam_threshold_seconds:15}}") long minIntervalSeconds,
                             @Value("${resend_rate_limit_ip_max:10}") int maxPerIp) {
        this.minIntervalMillis = minIntervalSeconds * 1000L;
        this.maxPerIp = maxPerIp;
    }

    public long getMinIntervalMillis() {
        return minIntervalMillis;
    }

    /**
     * @param key a namespaced key that can be used once per interval, or null for none
     * @param ip  the source IP, or null for none
     */
    public synchronized void checkAllowed(String key, String ip) {
        if (minIntervalMillis <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        if (key != null) {
            Long last = lastAllowedAt.getIfPresent(key);
            if (last != null && now < last + minIntervalMillis) {
                throw new ForbiddenException("Resend forbidden due to rate limit");
            }
        }
        IpWindow window = null;
        if (ip != null) {
            window = ipWindows.getIfPresent(ip);
            if (window == null || now >= window.windowStart + minIntervalMillis) {
                window = new IpWindow();
                window.windowStart = now;
                ipWindows.put(ip, window);
            }
            if (window.count >= maxPerIp) {
                throw new ForbiddenException("Resend forbidden due to rate limit");
            }
        }
        if (key != null) {
            lastAllowedAt.put(key, now);
        }
        if (window != null) {
            window.count++;
        }
    }
}
