package surfid.api;

import io.restassured.filter.Filter;
import io.restassured.filter.cookie.CookieFilter;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import surfid.AbstractIntegrationTest;
import surfid.model.UserResponse;

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
                .get("/surfid/api/sp/me")
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
                .header("Location", "http://localhost:" + port + "/startSSO?redirect_url=http://localhost:3000/redirect");
    }

    @Test
    public void startSSO() {
        Filter cookieFilter = new CookieFilter();
        given().redirects().follow(false)
                .when()
                .filter(cookieFilter)
                .param("redirect_url", "http://localhost/redirect")
                .get("/startSSO")
                .then()
                .statusCode(302)
                .header("Location", "http://localhost/redirect");

        UserResponse userResponse = given()
                .filter(cookieFilter)
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .get("/surfid/api/sp/me")
                .body()
                .as(UserResponse.class);

        assertEquals("jdoe@example.com", userResponse.getEmail());
        assertEquals("John", userResponse.getGivenName());
        assertEquals("Doe", userResponse.getFamilyName());
        assertFalse(userResponse.isHasPassword());
        assertNotNull(userResponse.getId());
    }

    @Test
    public void config() {
        given()
                .when()
                .get("/config")
                .then()
                .body("loginUrl", equalTo("http://localhost:8081/login"));
    }

}