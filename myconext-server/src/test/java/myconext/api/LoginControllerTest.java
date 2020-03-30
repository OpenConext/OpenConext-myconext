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
        String location = "http://localhost?query=123";
        given()
                .redirects().follow(false)
                .when()
                .queryParam("location", location)
                .get("/register")
                .then()
                .statusCode(302)
                .header("Location", location);
    }
}