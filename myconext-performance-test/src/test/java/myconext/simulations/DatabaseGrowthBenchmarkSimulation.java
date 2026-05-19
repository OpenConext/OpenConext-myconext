package myconext.simulations;

import io.gatling.javaapi.core.PopulationBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import myconext.chains.MyConextUserFlow;
import myconext.protocols.Protocols;
import myconext.reporting.GatlingComparisonReporter;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;

public class DatabaseGrowthBenchmarkSimulation extends Simulation {

    private static final int USERS_PER_RUN = Integer.getInteger("benchmarkUsers", 100);
    private static final int RAMP_SECONDS = Integer.getInteger("benchmarkRampSeconds", 30);
    private static final String AMOUNT_USERS =
            System.getProperty("amountusers", System.getProperty("amountUsers", ""));

    private final ScenarioBuilder benchmarkScenario = scenario("Remote Creation API Database Growth Benchmark")
            .exec(MyConextUserFlow.creationFlow);

    {
        GatlingComparisonReporter.registerShutdownHook(
                DatabaseGrowthBenchmarkSimulation.class.getSimpleName(),
                AMOUNT_USERS
        );

        PopulationBuilder population;
        if (RAMP_SECONDS <= 0) {
            population = benchmarkScenario.injectOpen(atOnceUsers(USERS_PER_RUN));
        } else {
            population = benchmarkScenario.injectOpen(rampUsers(USERS_PER_RUN).during(Duration.ofSeconds(RAMP_SECONDS)));
        }

        setUp(population).protocols(Protocols.httpProtocol);
    }
}
