package myconext.shibboleth;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import myconext.AbstractIntegrationTest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
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

@ActiveProfiles(value = "shib", inheritProfiles = false)
public class ShibbolethPreAuthenticatedProcessingFilterTest extends AbstractIntegrationTest {

    @Value("${onegini_entity_id}")
    private String oneGiniEntityId;

    @Test
    public void getPreAuthenticatedPrincipal() {
        Headers headers = headers(UUID.randomUUID().toString(), "steven.doe@example.org", oneGiniEntityId);
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
        assertEquals("http://mock-idp", user.getAuthenticatingAuthority());
    }

    @Test
    public void getPreAuthenticatedPrincipalMissingAttributes() {
        Headers headers = headers(UUID.randomUUID().toString(), "steven.doe@example.org", oneGiniEntityId);
        List<Header> headersAsList = new ArrayList<>(headers.asList());
        headersAsList.removeIf(header -> header.getName().equals(SHIB_EMAIL));
        headers = new Headers(headersAsList);
        given()
                .headers(headers)
                .when()
                .get("/myconext/api/sp/me")
                .then()
                .statusCode(403);
    }

    @Test
    public void migrationConflict() {
        Headers headers = headers(UUID.randomUUID().toString(), "jdoe@example.com", oneGiniEntityId);
        given()
                .headers(headers)
                .when()
                .get("/myconext/api/sp/me")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("email", equalTo("jdoe@example.com"));
    }

    @Test
    public void migrationConflictMerged() {
        String uid = UUID.randomUUID().toString();
        Headers headers = headers(uid, "jdoe@example.com", oneGiniEntityId);
        given()
                .headers(headers)
                .when()
                .get("/myconext/api/sp/migrate/merge")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("email", equalTo("jdoe@example.com"));
        assertEquals("jdoe@example.com", userRepository.findUserByUid(uid).get().getEmail());
    }

    @Test
    public void migrationConflictProceed() {
        String uid = UUID.randomUUID().toString();
        Headers headers = headers(uid, "jdoe@example.com", oneGiniEntityId);
        given()
                .headers(headers)
                .when()
                .get("/myconext/api/sp/migrate/proceed")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("email", equalTo("jdoe@example.com"));
        assertEquals(false, userRepository.findUserByUid(uid).isPresent());
    }

    private Headers headers(String uid, String email, String authenticatingAuthority) {
        return new Headers(
                new Header(SHIB_UID, uid),
                new Header(SHIB_SCHAC_HOME_ORGANIZATION, "surfguest.nl"),
                new Header(SHIB_GIVEN_NAME, "Steven"),
                new Header(SHIB_SUR_NAME, "Doe"),
                new Header(SHIB_AUTHENTICATING_AUTHORITY, authenticatingAuthority + " ; " + authenticatingAuthority),
                new Header(SHIB_EMAIL, email)
        );
    }
}