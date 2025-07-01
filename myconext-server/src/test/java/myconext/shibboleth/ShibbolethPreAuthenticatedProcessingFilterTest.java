package myconext.shibboleth;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import myconext.AbstractIntegrationTest;
import myconext.model.ExternalUser;
import myconext.model.User;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.*;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;

@ActiveProfiles(value = "shib", inheritProfiles = false)
public class ShibbolethPreAuthenticatedProcessingFilterTest extends AbstractIntegrationTest {

    @Test
    public void getPreAuthenticatedPrincipal() {
        Headers headers = headers(UUID.randomUUID().toString(), "steven.doe@example.org", "mijn.test2.eduid.nl");
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
        assertEquals("surfguest.nl", user.getSchacHomeOrganization());
    }

    @Test
    public void getPreAuthenticatedPrincipalServiceDesk() {
        String mail = "servicedesk.admin@surf.org";
        String uid = UUID.randomUUID().toString();
        Headers headers = headers(uid, mail, "servicedesk.test2.eduid.nl");

        given()
                .headers(headers)
                .when()
                .get("/myconext/api/servicedesk/me")
                .then()
                .body("email", equalTo(mail))
                .body("givenName", equalTo("Steven"))
                .body("familyName", equalTo("Doe"))
                .body("name", equalTo("Steven Doe"))
                .body("familyName", equalTo("Doe"))
                .body("serviceDeskMember", equalTo(true))
                .body("id", notNullValue());

        ExternalUser externalUser = super.externalUserRepository.findUserByUid(uid).get();
        assertEquals(mail, externalUser.getEmail());
    }

    @Test
    public void getPreAuthenticatedPrincipalUnknownHost() {
        String mail = "servicedesk.admin@surf.org";
        String uid = UUID.randomUUID().toString();
        Headers headers = headers(uid, mail, "nope.test2.eduid.nl");

        Map<String, Object> res = given()
                .headers(headers)
                .when()
                .get("/myconext/api/servicedesk/me")
                .as(new TypeRef<>() {
                });
        assertEquals(400, res.get("status"));
        assertEquals(res.get("message"), "Unknown host header in the request: nope.test2.eduid.nl");
    }

    @Test
    public void getPreAuthenticatedPrincipalUnknownAuthenticatingAuthority() {
        Headers headers = headers(UUID.randomUUID().toString(), "bob.doe@example.org", "mijn.test2.eduid.nl");
        given()
                .headers(headers)
                .when()
                .get("/myconext/api/sp/me")
                .then()
                .statusCode(200);

        User user = super.userRepository.findOneUserByEmail("bob.doe@example.org");
        assertEquals("surfguest.nl", user.getSchacHomeOrganization());
    }

    @Test
    public void getPreAuthenticatedPrincipalMissingAttributes() {
        Headers headers = headers(UUID.randomUUID().toString(), "steven.doe@example.org", "mijn.test2.eduid.nl");
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

    private Headers headers(String uid, String email, String host) {
        return new Headers(
                new Header("host", host),
                new Header(SHIB_UID, uid),
                new Header(SHIB_SCHAC_HOME_ORGANIZATION, "surfguest.nl"),
                new Header(SHIB_GIVEN_NAME, "Steven"),
                new Header(SHIB_SUR_NAME, "Doe"),
                new Header(SHIB_EMAIL, email),
                new Header(SHIB_MEMBERSHIPS, "role1")
        );
    }
}