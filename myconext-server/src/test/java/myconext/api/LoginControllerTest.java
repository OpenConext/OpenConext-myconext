package myconext.api;

import myconext.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

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
        given()
                .redirects().follow(false)
                .when()
                .get("/register")
                .then()
                .statusCode(302)
                .header("Location",
                        "https://my.test2.surfconext.nl/Shibboleth.sso/Login?entityID=https://localhost.surf.id&lang=en");
    }

    @Test
    public void registerWithCustomLocation() {
        String location = "/register?location=https%3A//connect.test2.surfconext.nl/oidc/authorize%3Flocation%3Dstate%3D%5B%22%5B%5C%22%5C%22%5D%22%2C%201%2C%20%22%22%2C%20%22%22%2C%20%22%22%5D%26client_id%3Dedubadges%26response_type%3Dcode%26scope%3Dopenid%26redirect_uri%3Dhttp%3A//localhost%3A8000/account/eduid/login/callback/";
        given()
                .redirects().follow(false)
                .when()
                .get(location)
                .then()
                .statusCode(302)
                .header("Location", "https://connect.test2.surfconext.nl/oidc/authorize?location=state=[\"[\\\"\\\"]\", 1, \"\", \"\", \"\"]&client_id=edubadges&response_type=code&scope=openid&redirect_uri=http://localhost:8000/account/eduid/login/callback/");
    }
}