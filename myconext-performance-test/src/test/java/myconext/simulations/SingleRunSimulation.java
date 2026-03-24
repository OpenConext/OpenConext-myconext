package myconext.simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import myconext.chains.MyConextUserFlow;
import myconext.protocols.Protocols;

import static io.gatling.javaapi.core.CoreDsl.*;

public class SingleRunSimulation extends Simulation {

    private ScenarioBuilder scenario = scenario("Single Run Test")
            .exec(MyConextUserFlow.creationFlow);

    {
        setUp(
                scenario.injectOpen(
                        atOnceUsers(1)
                )
        ).protocols(Protocols.httpProtocol)
                .assertions(
                        global().responseTime().max().lt(5000),
                        global().successfulRequests().percent().is(100.0)
                );
    }
}