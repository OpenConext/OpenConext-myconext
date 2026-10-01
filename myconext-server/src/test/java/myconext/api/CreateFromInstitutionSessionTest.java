package myconext.api;

import myconext.AbstractIntegrationTest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Without the LocalDevelopmentAuthenticationFilter, which fakes an OIDC user in the test profile, the session
 * created by the create-from-institution-login must be accepted by the API used by the mijn GUI.
 */
@TestPropertySource(properties = "local_development_authentication=false")
public class CreateFromInstitutionSessionTest extends AbstractIntegrationTest {

    @Test
    public void sessionAfterCreateFromInstitutionLoginCanCallMe() {
        User user = userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com").get();
        String key = UUID.randomUUID().toString();
        user.setCreateFromInstitutionKey(key);
        user.setNewUser(true);
        userRepository.save(user);

        //Sanity check: without a session the API is protected
        given()
                .redirects().follow(false)
                .when()
                .get("/myconext/api/sp/me")
                .then()
                .statusCode(302);

        String session = given()
                .redirects().follow(false)
                .queryParam("key", key)
                .when()
                .get("/create-from-institution-login")
                .then()
                .statusCode(302)
                .extract().cookie("SESSION");

        given()
                .cookie("SESSION", session)
                .when()
                .get("/myconext/api/sp/me")
                .then()
                .statusCode(200)
                .body("email", equalTo(user.getEmail()));
    }
}
