package myconext.simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import myconext.chains.EduID;
import myconext.protocols.Protocols;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Gradual Ramp Up Test - 8 minutes
 * Purpose: Safe initial testing, find baseline performance
 * Use: First time testing, establishing baseline metrics
 * -------------------
 * Total requests: ~4,800 (10 users/sec × 60 sec × 5 min + ramp periods)
 * Good for: Baseline metrics, initial capacity assessment
 */
public class GradualRampUpSimulation extends Simulation {
    private ScenarioBuilder scenario = scenario("Gradual Ramp Up")
            .exec(EduID.creationFlow);

    {
        setUp(
                scenario.injectOpen(
                        // Warm up: 1-10 users/sec over 2 minutes
                        rampUsersPerSec(1).to(10).during(Duration.ofMinutes(2)),
                        // Sustained load: 10 users/sec for 5 minutes
                        constantUsersPerSec(10).during(Duration.ofMinutes(5)),
                        // Cool down: 10-1 users/sec over 1 minute
                        rampUsersPerSec(10).to(1).during(Duration.ofMinutes(1))
                )
        ).protocols(Protocols.httpProtocol)
                .assertions(
                        global().responseTime().max().lt(5000),
                        global().responseTime().mean().lt(2000),
                        global().successfulRequests().percent().gt(95.0)
                );
    }
}
