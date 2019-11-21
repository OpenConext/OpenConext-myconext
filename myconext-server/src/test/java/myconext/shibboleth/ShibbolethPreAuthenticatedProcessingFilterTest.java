package myconext.shibboleth;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import myconext.AbstractIntegrationTest;

import java.io.UnsupportedEncodingException;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.SHIB_EMAIL;
import static myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.SHIB_GIVEN_NAME;
import static myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.SHIB_SUR_NAME;

@ActiveProfiles(value = "dev", inheritProfiles = false)
public class ShibbolethPreAuthenticatedProcessingFilterTest extends AbstractIntegrationTest {

    @Test
    public void getPreAuthenticatedPrincipal() throws UnsupportedEncodingException {
        Headers headers = new Headers(
                new Header(SHIB_GIVEN_NAME, "Steven"),
                new Header(SHIB_SUR_NAME, "Doe"),
                new Header(SHIB_EMAIL, "steven.doe@example.org")
        );
        given()
                .headers(headers)
                .when()
                .get("/surfid/api/sp/me")
                .then()
                .body("email", equalTo("steven.doe@example.org"))
                .body("givenName", equalTo("Steven"))
                .body("familyName", equalTo("Doe"))
                .body("usePassword", equalTo(false))
                .body("id", notNullValue());

    }
}