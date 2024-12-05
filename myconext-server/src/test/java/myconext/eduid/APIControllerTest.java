package myconext.eduid;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.*;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@SuppressWarnings("unchecked")
public class APIControllerTest extends AbstractIntegrationTest {

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8098);

    @Test
    public void eppn() throws Exception {
        List<Map<String, String>> results = given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/eppn"))
                .get("/myconext/api/eduid/eppn")
                .as(List.class);
        String eppn = results.get(1).get("eppn");
        assertEquals("1234567890@surfguest.nl", eppn);
    }

    @Test
    public void eduid() throws Exception {
        Map<String, String> results = given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/eduid"))
                .get("/myconext/api/eduid/eduid")
                .as(Map.class);
        assertEquals("fc75dcc7-6def-4054-b8ba-3c3cc504dd4b", results.get("eduid"));
    }

    @Test
    public void links() throws Exception {
        List<Map<String, String>> results = given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/links"))
                .get("/myconext/api/eduid/links")
                .as(List.class);
        String validatedName = results.get(0).get("validated_name");
        assertEquals("Mary Dahl", validatedName);

        String eppn = results.get(1).get("eppn");
        assertEquals("1234567890@surfguest.nl", eppn);
    }

    @Test
    public void linksExternalAccount() throws Exception {
        User user = userRepository.findUserByEmail("jdoe@example.com").get();
        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount(
                UUID.randomUUID().toString(),
                IdpScoping.eherkenning,
                new VerifyIssuer(IdpScoping.eherkenning.name(), IdpScoping.eherkenning.name(), null),
                Verification.Geverifieerd,
                UUID.randomUUID().toString(),
                IdpScoping.studielink.name(),
                IdpScoping.studielink.name(),
                null,
                null,
                "Johny",
                "Harry",
                "Toe",
                "Toe",
                null,
                null,
                null,
                null,
                new Date(),
                new Date(),
                Date.from(Instant.now().plus(365 * 5, ChronoUnit.DAYS)),
                true
        );
        externalLinkedAccount.setPreferred(true);
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        userRepository.save(user);

        List<Map<String, String>> results = given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/links"))
                .get("/myconext/api/eduid/links")
                .as(List.class);

        Map<String, String> info = results.get(2);
        assertFalse(info.containsKey("eppn"));
        assertFalse(info.containsKey("schac_home_organization"));

        String validatedName = info.get("validated_name");
        assertEquals("Harry Toe", validatedName);

    }

    @Test
    public void eppnBySchacHome() throws Exception {
        List<Map<String, String>> results = given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/eppn"))
                .queryParam("schachome", "groningen.nl")
                .get("/myconext/api/eduid/eppn")
                .as(List.class);
        String value = results.get(0).get("eppn");
        assertEquals("1234567890@surfguest.nl", value);
    }

    @Test
    public void eppnInvalidToken() throws Exception {
        given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(false))
                .get("/myconext/api/eduid/eppn")
                .then()
                .statusCode(401);
    }

    @Test
    public void eppnInvalidUid() throws Exception {
        given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(doOpaqueAccessToken(true, new String[]{"eduid.nl/eppn"}, "introspect_invalid_user"))
                .get("/myconext/api/eduid/eppn")
                .then()
                .statusCode(404);
    }

    @Test
    public void missingUid() throws Exception {
        List<Map<String, String>> results = given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(doOpaqueAccessToken(true, new String[]{"eduid.nl/eppn"}, "introspect_missing_uid"))
                .get("/myconext/api/eduid/eppn")
                .as(List.class);
        String value = results.get(1).get("eppn");
        assertEquals("1234567890@surfguest.nl", value);
    }

    @Test
    public void eppnInvalidScope() throws Exception {
        given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "invalid-scope"))
                .get("/myconext/api/eduid/eppn")
                .then()
                .statusCode(403);
    }

    @Test
    public void eppnNoLinkedAccounts() throws Exception {
        List<Map<String, String>> results = given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessTokenWithNoLinkedAccount("eduid.nl/eppn"))
                .get("/myconext/api/eduid/eppn")
                .as(List.class);
        assertEquals(0, results.size());
    }

}