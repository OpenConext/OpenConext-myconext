package myconext.simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Gradual Ramp Up Test - 8 minutes
 * Purpose: Safe initial testing, find baseline performance
 * Use: First time testing, establishing baseline metrics
 * -------------------
 * Total requests: ~4,800 (10 users/sec × 60 sec × 5 min + ramp periods)
 * Good for: Baseline metrics, initial capacity assessment
 */
public class GradualRampUpSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8081");
    private static final String USERNAME = System.getProperty("username", "studielink");
    private static final String PASSWORD = System.getProperty("password", "secret");

    private HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .basicAuth(USERNAME, PASSWORD);

    private ChainBuilder eduIDCreationFlow = exec(session -> {
        String email = "user-" + UUID.randomUUID().toString() + "@test.com";
        String identifier = UUID.randomUUID().toString();
        System.out.println("=== Starting flow for email: " + email + " ===");
        return session
                .set("email", email)
                .set("identifier", identifier);
    })
            // Step 1: Check if email exists (should return 404)
            .exec(
                    http("Check Email Does Not Exist")
                            .get("/api/remote-creation/email-eduid-exists")
                            .queryParam("email", "#{email}")
                            .check(status().is(404))
            )
            .exec(session -> {
                System.out.println("✓ Step 1 completed: Email check for " + session.getString("email"));
                return session;
            })
            .pause(Duration.ofMillis(100), Duration.ofMillis(500))
            // Step 2: Create new EduID with Ongeverifieerd status
            .exec(
                    http("Create EduID - Ongeverifieerd")
                            .post("/api/remote-creation/eduid-create")
                            .body(StringBody("""
                    {
                      "email": "#{email}",
                      "eduIDValue": null,
                      "firstName": "Mary",
                      "chosenName": "Mary",
                      "lastNamePrefix": "von",
                      "lastName": "Munich",
                      "dateOfBirth": "19880327",
                      "identifier": "#{identifier}",
                      "verification": "Ongeverifieerd",
                      "brinCodes": null
                    }
                    """)).asJson()
                            .check(status().is(201))
                            .check(jsonPath("$.eduIDValue").saveAs("eduIDValue"))
            )
            .exec(session -> {
                System.out.println("✓ Step 2 completed: EduID created with ID " + session.getString("eduIDValue"));
                return session;
            })
            .pause(Duration.ofMillis(100), Duration.ofMillis(500))
            // Step 3: Update - use data from step 2, change verification + add brinCodes
            .exec(
                    http("Update EduID - Geverifieerd")
                            .put("/api/remote-creation/eduid-update")
                            .body(StringBody("""
                    {
                      "email": "#{email}",
                      "eduIDValue": "#{eduIDValue}",
                      "firstName": "Hedwig",
                      "chosenName": "Hadda",
                      "lastNamePrefix": "bis",
                      "lastName": "Marken",
                      "dateOfBirth": "19880327",
                      "identifier": "#{identifier}",
                      "verification": "Geverifieerd",
                      "brinCodes": ["ST42"]
                    }
                    """)).asJson()
                            .check(status().is(201))
            )
            .exec(session -> {
                System.out.println("✓ Step 3 completed: EduID updated to Geverifieerd");
                System.out.println("=== Flow completed successfully for " + session.getString("email") + " ===\n");
                return session;
            });

    private ScenarioBuilder scenario = scenario("Gradual Ramp Up")
            .exec(eduIDCreationFlow);

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
        ).protocols(httpProtocol)
                .assertions(
                        global().responseTime().max().lt(5000),
                        global().responseTime().mean().lt(2000),
                        global().successfulRequests().percent().gt(95.0)
                );
    }
}
