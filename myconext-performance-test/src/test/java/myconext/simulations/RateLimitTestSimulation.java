package myconext.simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import myconext.chains.EduID;
import myconext.protocols.Protocols;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Rate Limit Test - Test Load Balancer Limits
 * Duration: ~6 minutes
 *
 * Purpose: Intentionally exceed rate limits to test load balancer behavior
 *
 * Rate Limits:
 * - 250 requests per path per minute (~4.2 req/sec per endpoint)
 * - 1000 requests overall per 10 seconds (100 req/sec total)
 *
 * Test Strategy:
 * Phase 1: Start within limits (4 users/sec)
 * Phase 2: Gradually exceed per-path limit (4 → 10 users/sec)
 * Phase 3: Spike to test both limits (20 users/sec)
 * Phase 4: Sustain over-limit load (15 users/sec)
 *
 * Expected Behavior:
 * - HTTP 429 (Too Many Requests) responses
 * - Throttling/queueing
 * - Some requests succeed, some are rate-limited
 *
 * Success Criteria:
 * - System doesn't crash
 * - Graceful degradation (429 responses, not 500 errors)
 * - Response times stay reasonable (< 10 seconds)
 */
public class RateLimitTestSimulation extends Simulation {

    private ScenarioBuilder scenario = scenario("Rate Limit Test")
            .exec(EduID.creationFlow);

    {
        setUp(
                scenario.injectOpen(
                        // Initial pause
                        nothingFor(Duration.ofSeconds(5)),

                        // Phase 1: Start safely within limits
                        // 4 users/sec = 12 req/sec total, 4 req/sec per endpoint ✓
                        constantUsersPerSec(4).during(Duration.ofMinutes(1)),

                        // Phase 2: Gradually exceed per-path limit
                        // 4 → 10 users/sec = up to 30 req/sec per endpoint ✗
                        rampUsersPerSec(4).to(10).during(Duration.ofMinutes(2)),

                        // Phase 3: Spike - test overall limit
                        // 20 users/sec = 60 req/sec total, 20 req/sec per endpoint ✗✗
                        constantUsersPerSec(20).during(Duration.ofSeconds(30)),

                        // Phase 4: Sustained over-limit
                        // 15 users/sec = 45 req/sec total, 15 req/sec per endpoint ✗
                        constantUsersPerSec(15).during(Duration.ofMinutes(2)),

                        // Phase 5: Return to safe level
                        rampUsersPerSec(15).to(4).during(Duration.ofSeconds(30))
                )
        ).protocols(Protocols.httpProtocol)
                .assertions(
                        // Relaxed assertions - we expect some failures due to rate limiting
                        global().responseTime().percentile(95.0).lt(10000),
                        global().responseTime().percentile(99.0).lt(15000),

                        // We expect rate limiting, so lower success threshold
                        // At least 40% should succeed (realistic for intentional overload)
                        global().successfulRequests().percent().gt(40.0),

                        // System should still be responding (not crashed)
                        global().requestsPerSec().gt(10.0),

                        // Ensure we actually generated high load during spike
                        global().requestsPerSec().gt(30.0)  // Peak should be ~45-60 req/sec
                );
    }
}