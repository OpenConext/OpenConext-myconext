package myconext.eduid;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

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

    private String doOpaqueAccessToken(boolean valid, String[] scopes, String filePart) throws IOException {
        List<String> scopeList = new ArrayList<>(Arrays.asList(scopes));
        scopeList.add("openid");

        String file = String.format("oidc/%s.json", valid ? filePart : "introspect-invalid-token");
        String introspectResult = IOUtils.toString(new ClassPathResource(file).getInputStream(), Charset.defaultCharset());
        String introspectResultWithScope = valid ? String.format(introspectResult, String.join(" ", scopeList)) : introspectResult;
        stubFor(post(urlPathMatching("/introspect")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(introspectResultWithScope)));
        return UUID.randomUUID().toString();
    }

    private String opaqueAccessTokenWithNoLinkedAccount(String... scopes) throws IOException {
        return doOpaqueAccessToken(true, scopes, "introspect_no_linked_accounts");
    }

    private String opaqueAccessToken(boolean valid, String... scopes) throws IOException {
        return doOpaqueAccessToken(valid, scopes, "introspect");
    }

}