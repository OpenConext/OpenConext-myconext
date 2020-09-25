package myconext.api;

import myconext.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static myconext.security.GuestIdpAuthenticationRequestFilter.REGISTER_MODUS_COOKIE_NAME;
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
                .body("eduIDRegisterUrl", equalTo("http://localhost:3000/register"))
                .body("eduIDDoLoginUrl", equalTo("http://localhost:3000/doLogin"))
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
                .cookie(REGISTER_MODUS_COOKIE_NAME,"true")
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
                .cookie(REGISTER_MODUS_COOKIE_NAME,"true")
                .statusCode(302)
                .header("Location", location);
    }

    @Test
    public void doLogin() {
        given()
                .redirects().follow(false)
                .when()
                .get("/doLogin")
                .then()
                .statusCode(302)
                .cookie(REGISTER_MODUS_COOKIE_NAME,"")
                .header("Location",
                        "https://my.test2.surfconext.nl/Shibboleth.sso/Login?entityID=https://localhost.surf.id&lang=en");
    }

    @Test
    public void doLoginWithCustomLocation() {
        String location = "http://localhost?query=123";
        given()
                .redirects().follow(false)
                .when()
                .queryParam("location", location)
                .get("/doLogin")
                .then()
                .cookie(REGISTER_MODUS_COOKIE_NAME,"")
                .statusCode(302)
                .header("Location", location);
    }
}