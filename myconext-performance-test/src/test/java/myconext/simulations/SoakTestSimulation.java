package myconext.simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import myconext.chains.EduID;
import myconext.protocols.Protocols;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Soak Test - 35 minutes
 * Purpose: Identify memory leaks, resource exhaustion, degradation over time
 * Use: Production readiness, detect slow resource leaks
 *
 * Rate Limits:
 * - 250 requests per path per minute (~4.2 req/sec per endpoint)
 * - 1000 requests overall per 10 seconds (100 req/sec total)
 *
 * This test stays WITHIN limits for long-term stability:
 * - 3 users/sec = 9 req/sec total ✓
 * - 3 req/sec per endpoint ✓ (comfortably under 4.2/sec)
 *
 * Key Goal: System remains stable for 30+ minutes WITHOUT degradation
 *
 * Total requests: ~5,400 (3 users/sec × 60 sec × 30 min)
 *
 * Expected Result:
 * - Consistent performance throughout
 * - No memory leaks
 * - No gradual slowdown
 * - 99%+ success rate maintained
 */
public class SoakTestSimulation extends Simulation {

    private ScenarioBuilder scenario = scenario("Soak Test - Long Duration Stability")
            .exec(EduID.creationFlow);

    {
        setUp(
                scenario.injectOpen(
                        // Gradual ramp to target load: 1-3 users/sec over 5 minutes
                        rampUsersPerSec(1).to(3).during(Duration.ofMinutes(5)),

                        // Sustained load: 3 users/sec for 30 minutes
                        // This is the KEY - long duration at moderate load
                        constantUsersPerSec(3).during(Duration.ofMinutes(30))
                )
        ).protocols(Protocols.httpProtocol)
                .assertions(
                        // Response times should remain stable throughout
                        global().responseTime().percentile(95.0).lt(4000),
                        global().responseTime().percentile(99.0).lt(5000),
                        global().responseTime().mean().lt(2000),

                        // Near-perfect success rate (we're well within limits)
                        global().successfulRequests().percent().gt(99.0),

                        // CRITICAL for soak test: Check for degradation over time
                        // All requests should maintain good performance
                        forAll().responseTime().percentile(95.0).lt(4000),
                        forAll().successfulRequests().percent().gt(98.0),

                        // Ensure no failed requests accumulate
                        global().failedRequests().count().lt(50L),  // < 50 failures in 30 min

                        // Verify consistent throughput (should be ~9 req/sec)
                        global().requestsPerSec().gt(7.0),
                        global().requestsPerSec().lt(11.0)  // Shouldn't spike unexpectedly
                );
    }
}