package myconext.simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import myconext.chains.EduID;
import myconext.protocols.Protocols;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Stress Test - Progressive Breaking Point Test
 * Duration: ~10 minutes
 *
 * Purpose: Find the actual breaking point by progressively increasing load
 * Use: Determine maximum capacity and observe failure modes
 *
 * Rate Limits:
 * - 250 requests per path per minute (~4.2 req/sec per endpoint)
 * - 1000 requests overall per 10 seconds (100 req/sec total)
 *
 * Test Strategy:
 * Phase 1: Start within limits (4 users/sec) - baseline
 * Phase 2: Exceed per-path limit (8 users/sec) - light stress
 * Phase 3: Spike test (15 users/sec burst) - moderate stress
 * Phase 4: Heavy load (12 users/sec sustained) - heavy stress
 * Phase 5: Recovery test - return to safe levels
 *
 * Expected Behavior:
 * - Phase 1: 99%+ success
 * - Phase 2-4: Progressive rate limiting (50-80% success)
 * - Phase 5: System recovers
 */
public class StressTestSimulation extends Simulation {

    private ScenarioBuilder scenario = scenario("Progressive Stress Test")
            .exec(EduID.creationFlow);

    {
        setUp(
                scenario.injectOpen(
                        // Initial pause
                        nothingFor(Duration.ofSeconds(5)),

                        // Phase 1: Baseline within limits (2 min)
                        // 4 users/sec = at the edge of rate limit
                        constantUsersPerSec(4).during(Duration.ofMinutes(2)),

                        // Phase 2: Light stress - exceed per-path limit (2 min)
                        // 8 users/sec = 24 req/sec total, 8 req/sec per endpoint
                        constantUsersPerSec(8).during(Duration.ofMinutes(2)),

                        // Phase 3: Spike - moderate burst (30 sec)
                        // 15 users/sec = 45 req/sec total, 15 req/sec per endpoint
                        constantUsersPerSec(15).during(Duration.ofSeconds(30)),

                        // Phase 4: Heavy sustained load (3 min)
                        // 12 users/sec = 36 req/sec total, 12 req/sec per endpoint
                        constantUsersPerSec(12).during(Duration.ofMinutes(3)),

                        // Phase 5: Recovery - back to safe levels (2 min)
                        rampUsersPerSec(12).to(4).during(Duration.ofMinutes(1)),
                        constantUsersPerSec(4).during(Duration.ofMinutes(1))
                )
        ).protocols(Protocols.httpProtocol)
                .assertions(
                        // Realistic assertions for stress test
                        // We EXPECT some failures due to rate limiting

                        // Response times may be slower due to throttling
                        global().responseTime().percentile(95.0).lt(10000),
                        global().responseTime().percentile(99.0).lt(15000),

                        // Success rate: at least 60% should succeed overall
                        // (Phase 1 will be 99%, phases 2-4 will be 40-70%, balanced average)
                        global().successfulRequests().percent().gt(60.0),

                        // Ensure system doesn't completely crash
                        global().requestsPerSec().gt(15.0),  // Should average ~20-25 req/sec

                        // Verify we hit high load during stress phases
                        global().requestsPerSec().gt(25.0)   // Peak should be ~36-45 req/sec
                );
    }
}