package myconext.shibboleth;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import myconext.AbstractIntegrationTest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.SHIB_AUTHENTICATING_AUTHORITY;
import static myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.SHIB_EMAIL;
import static myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.SHIB_GIVEN_NAME;
import static myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.SHIB_SCHAC_HOME_ORGANIZATION;
import static myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.SHIB_SUR_NAME;
import static myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.SHIB_UID;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;

@ActiveProfiles(value = "dev", inheritProfiles = false)
public class ShibbolethPreAuthenticatedProcessingFilterTest extends AbstractIntegrationTest {

    @Test
    public void getPreAuthenticatedPrincipal() {
        Headers headers = new Headers(
                new Header(SHIB_UID, UUID.randomUUID().toString()),
                new Header(SHIB_SCHAC_HOME_ORGANIZATION, "surfguest.nl"),
                new Header(SHIB_GIVEN_NAME, "Steven"),
                new Header(SHIB_SUR_NAME, "Doe"),
                new Header(SHIB_AUTHENTICATING_AUTHORITY, "http://mock-sp ; http://mock-sp"),
                new Header(SHIB_EMAIL, "steven.doe@example.org")
        );
        given()
                .headers(headers)
                .when()
                .get("/myconext/api/sp/me")
                .then()
                .body("email", equalTo("steven.doe@example.org"))
                .body("givenName", equalTo("Steven"))
                .body("familyName", equalTo("Doe"))
                .body("usePassword", equalTo(false))
                .body("rememberMe", equalTo(false))
                .body("schacHomeOrganization", equalTo("surfguest.nl"))
                .body("id", notNullValue());

        User user = super.userRepository.findOneUserByEmail("steven.doe@example.org");
        assertEquals("http://mock-sp", user.getAuthenticatingAuthority());
    }
}