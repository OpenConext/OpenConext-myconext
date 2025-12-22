package myconext.simulations;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.time.Duration;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Stress Test - ~5 minutes
 * Purpose: Find breaking point and test error handling under extreme load
 * Use: Determine maximum capacity, test failover mechanisms
 * -------------------
 * Total requests: ~9,600+ (includes 100 instant + aggressive ramp)
 * Good for: Maximum capacity, error rate under pressure
 */
public class StressTestSimulation extends Simulation {

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

    private ScenarioBuilder scenario = scenario("Stress Test")
            .exec(eduIDCreationFlow);

    {
        setUp(
                scenario.injectOpen(
                        // Initial pause
                        nothingFor(Duration.ofSeconds(5)),
                        // Spike: sudden burst of 100 concurrent users
                        atOnceUsers(100),
                        // Aggressive ramp: 10-50 users/sec over 2 minutes
                        rampUsersPerSec(10).to(50).during(Duration.ofMinutes(2)),
                        // Peak load: 50 users/sec for 3 minutes
                        constantUsersPerSec(50).during(Duration.ofMinutes(3))
                )
        ).protocols(httpProtocol)
                .assertions(
                        global().responseTime().percentile(95.0).lt(5000),
                        global().responseTime().percentile(99.0).lt(10000),
                        global().successfulRequests().percent().gt(90.0)
                );
    }
}
