package myconext.simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import myconext.chains.EduID;
import myconext.protocols.Protocols;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Load Test - Within Rate Limits
 * Duration: ~8 minutes
 *
 * Purpose: Test normal production load while staying within load balancer limits
 *
 * Rate Limits:
 * - 250 requests per path per minute (~4.2 req/sec per endpoint)
 * - 1000 requests overall per 10 seconds (100 req/sec total)
 *
 * Our flow uses 3 endpoints:
 * - GET  /api/remote-creation/email-eduid-exists
 * - POST /api/remote-creation/eduid-create
 * - PUT  /api/remote-creation/eduid-update
 *
 * Safe limit: 4 users/sec × 3 requests = 12 req/sec total
 * Per endpoint: 4 req/sec (under 4.2/sec limit) ✓
 *
 * Expected Result: 100% success, fast responses
 */
public class LoadTestSimulation extends Simulation {

    private ScenarioBuilder scenario = scenario("Load Test - Within Limits")
            .exec(EduID.creationFlow);

    {
        setUp(
                scenario.injectOpen(
                        // Initial pause
                        nothingFor(Duration.ofSeconds(5)),

                        // Warm-up: slowly ramp 0 → 4 users/sec over 2 minutes
                        rampUsersPerSec(0).to(4).during(Duration.ofMinutes(2)),

                        // Sustained load: 4 users/sec for 5 minutes (within limits)
                        // = 12 req/sec total, 4 req/sec per endpoint
                        constantUsersPerSec(4).during(Duration.ofMinutes(5)),

                        // Cool down: 4 → 1 users/sec over 1 minute
                        rampUsersPerSec(4).to(1).during(Duration.ofMinutes(1))
                )
        ).protocols(Protocols.httpProtocol)
                .assertions(
                        // Strict assertions - should pass easily within limits
                        global().responseTime().percentile(95.0).lt(3000),
                        global().responseTime().percentile(99.0).lt(5000),
                        global().responseTime().mean().lt(2000),
                        global().successfulRequests().percent().gt(99.0),

                        // Zero failures expected when within limits
                        global().failedRequests().count().is(0L),

                        // Verify we're actually generating expected load
                        global().requestsPerSec().gt(10.0)  // Should be ~12 req/sec during peak
                );
    }
}