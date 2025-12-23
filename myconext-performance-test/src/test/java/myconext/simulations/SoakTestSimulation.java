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
 * -------------------
 * Total requests: ~36,000 (20 users/sec × 60 sec × 30 min + ramp)
 * Good for: Memory leaks, performance degradation over time
 */
public class SoakTestSimulation extends Simulation {

    private ScenarioBuilder scenario = scenario("Soak Test")
            .exec(EduID.creationFlow);

    {
        setUp(
                scenario.injectOpen(
                        // Gradual ramp to target load: 1-20 users/sec over 5 minutes
                        rampUsersPerSec(1).to(20).during(Duration.ofMinutes(5)),
                        // Sustained load: 20 users/sec for 30 minutes
                        constantUsersPerSec(20).during(Duration.ofMinutes(30))
                )
        ).protocols(Protocols.httpProtocol)
                .assertions(
                        global().responseTime().percentile(99.0).lt(5000),
                        global().responseTime().mean().lt(2000),
                        global().successfulRequests().percent().gt(99.0),
                        // Check for degradation: response times should remain stable
                        forAll().responseTime().percentile(95.0).lt(4000)
                );
    }
}
