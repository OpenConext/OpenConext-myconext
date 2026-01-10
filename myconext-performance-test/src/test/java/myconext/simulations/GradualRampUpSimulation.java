package myconext.simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import myconext.chains.EduID;
import myconext.protocols.Protocols;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Gradual Ramp Up Test - 8 minutes
 * Purpose: Safe initial testing, find baseline performance within rate limits
 * Use: First time testing, establishing baseline metrics
 *
 * Rate Limits:
 * - 250 requests per path per minute (~4.2 req/sec per endpoint)
 * - 1000 requests overall per 10 seconds (100 req/sec total)
 *
 * This test stays WITHIN limits:
 * - Max 4 users/sec = 12 req/sec total ✓
 * - 4 req/sec per endpoint ✓
 *
 * Expected Result: Near-perfect performance, 99%+ success
 */
public class GradualRampUpSimulation extends Simulation {
    private ScenarioBuilder scenario = scenario("Gradual Ramp Up - Baseline")
            .exec(EduID.creationFlow);

    {
        setUp(
                scenario.injectOpen(
                        // Warm up: 1-4 users/sec over 2 minutes (within limits)
                        rampUsersPerSec(1).to(4).during(Duration.ofMinutes(2)),

                        // Sustained baseline load: 4 users/sec for 5 minutes
                        // = 12 req/sec total, 4 req/sec per endpoint (at limit edge)
                        constantUsersPerSec(4).during(Duration.ofMinutes(5)),

                        // Cool down: 4-1 users/sec over 1 minute
                        rampUsersPerSec(4).to(1).during(Duration.ofMinutes(1))
                )
        ).protocols(Protocols.httpProtocol)
                .assertions(
                        // Strict assertions - should pass easily within limits
                        global().responseTime().max().lt(5000),
                        global().responseTime().mean().lt(2000),
                        global().responseTime().percentile(95.0).lt(3000),
                        global().successfulRequests().percent().gt(99.0),

                        // Ensure consistent performance across all requests
                        forAll().responseTime().mean().lt(2500),

                        // Ensure we're actually generating load
                        global().requestsPerSec().gt(10.0)  // Should be ~12 req/sec
                );
    }
}