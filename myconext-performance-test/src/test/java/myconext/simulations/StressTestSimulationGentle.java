package myconext.simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import myconext.chains.EduID;
import myconext.protocols.Protocols;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.*;

/**
 * Stress Test - ~5 minutes
 * Purpose: Find breaking point and test error handling under extreme load
 * Use: Determine maximum capacity, test failover mechanisms
 * -------------------
 * Total requests: ~9,600+ (includes 100 instant + aggressive ramp)
 * Good for: Maximum capacity, error rate under pressure
 */
public class StressTestSimulationGentle extends Simulation {

    private ScenarioBuilder scenario = scenario("Stress Test Gentle")
            .exec(EduID.creationFlow);

    {
        setUp(
                scenario.injectOpen(
                        // Initial pause
                        nothingFor(Duration.ofSeconds(5)),
                        // Spike: sudden burst of 100 concurrent users
                        atOnceUsers(100),
                        // Aggressive ramp: 10-50 users/sec over 2 minutes
                        rampUsersPerSec(1).to(10).during(Duration.ofMinutes(2)),
                        // Peak load: 50 users/sec for 3 minutes
                        constantUsersPerSec(10).during(Duration.ofMinutes(3))
                )
        ).protocols(Protocols.httpProtocol)
                .assertions(
                        global().responseTime().percentile(95.0).lt(5000),
                        global().responseTime().percentile(99.0).lt(10000),
                        global().successfulRequests().percent().gt(90.0)
                );
    }
}
