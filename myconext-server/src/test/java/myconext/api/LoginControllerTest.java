package myconext.api;

import io.restassured.filter.Filter;
import io.restassured.filter.cookie.CookieFilter;
import myconext.AbstractIntegrationTest;
import myconext.model.UserResponse;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@ActiveProfiles(value = "dev", inheritProfiles = false)
public class LoginControllerTest extends AbstractIntegrationTest {

    @Test
    public void secureMe() {
        given()
                .when()
                .get("/myconext/api/sp/me")
                .then()
                .statusCode(403);
    }

    @Test
    public void login() {
        given().redirects().follow(false)
                .when()
                .param("redirect_path", "/redirect")
                .get("/login")
                .then()
                .statusCode(302)
                .header("Location", "http://localhost:" + port + "/startSSO?redirect_url=http://localhost:3001/redirect");
    }

    @Test
    public void startSSO() {
        Filter cookieFilter = new CookieFilter();
        given().redirects().follow(false)
                .when()
                .filter(cookieFilter)
                .param("redirect_url", "http://localhost:3001/redirect")
                .get("/startSSO")
                .then()
                .statusCode(302)
                .header("Location", "http://localhost:3001/redirect");

        UserResponse userResponse = given()
                .filter(cookieFilter)
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .get("/myconext/api/sp/me")
                .body()
                .as(UserResponse.class);

        assertEquals("jdoe@example.com", userResponse.getEmail());
        assertEquals("John", userResponse.getGivenName());
        assertEquals("Doe", userResponse.getFamilyName());
        assertFalse(userResponse.isUsePassword());
        assertNotNull(userResponse.getId());
    }

    @Test
    public void startSSOInvalidRedirect() {
        Filter cookieFilter = new CookieFilter();
        given().redirects().follow(false)
                .when()
                .filter(cookieFilter)
                .param("redirect_url", "http://bogus")
                .get("/startSSO")
                .then()
                .statusCode(400);
    }

    @Test
    public void config() {
        given()
                .when()
                .get("/config")
                .then()
                .body("baseDomain", equalTo("test2.surfconext.nl"))
                .body("loginUrl", equalTo("http://localhost:8081/login"))
                .body("migrationUrl", equalTo("https://my.test2.surfconext.nl/Shibboleth.sso/Login?entityID=http://mock-idp&target=/startSSO?redirect_url=/migration"));
    }

}