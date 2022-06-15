package myconext.api;

import myconext.AbstractIntegrationTest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNull;

@ActiveProfiles(value = "dev", inheritProfiles = false)
public class LoginControllerTest extends AbstractIntegrationTest {

    @Test
    public void config() {
        given()
                .when()
                .get("/config")
                .then()
                .body("baseDomain", equalTo("test2.surfconext.nl"))
                .body("loginUrl", equalTo("http://localhost:8081/login"))
                .body("migrationUrl", equalTo(
                        "https://my.test2.surfconext.nl/Shibboleth.sso/Login?entityID=http://mock-idp&target=/migration"));
    }

    @Test
    public void register() {
        User user = userRepository.findUserByEmail("jdoe@example.com").get();
        String enrollmentVerificationKey = UUID.randomUUID().toString();
        user.setEnrollmentVerificationKey(enrollmentVerificationKey);
        userRepository.save(user);

        given()
                .redirects().follow(false)
                .when()
                .pathParam("enrollmentVerificationKey", enrollmentVerificationKey)
                .pathParam("timestamp", System.currentTimeMillis())
                .get("/register/{enrollmentVerificationKey}/{timestamp}")
                .then()
                .statusCode(302)
                .cookie("login_preference", "useApp")
                .cookie("username", user.getEmail())
                .header("Location",
                        "http://localhost:3001/security");

        user = userRepository.findUserByEmail("jdoe@example.com").get();
        assertNull(user.getEnrollmentVerificationKey());
    }

}