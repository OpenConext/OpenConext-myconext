package myconext.chains;

import io.gatling.javaapi.core.ChainBuilder;

import java.time.Duration;
import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

public class EduID {
    public static ChainBuilder creationFlow = exec(session -> {
        String email = "user-" + UUID.randomUUID() + "@test.com";
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

}
