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

public class APIControllerTest extends AbstractIntegrationTest {

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8098);

    @Test
    public void eppn() throws Exception {
        Map map = given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/eppn"))
                .get("/myconext/api/eduid/eppn")
                .as(Map.class);
        List<String> values = (List<String>) map.get("eppn");
        assertEquals("1234567890@surfguest.nl", values.get(0));
    }

    @Test
    public void eppnBySchacHome() throws Exception {
        Map map = given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/eppn"))
                .queryParam("schachome", "groningen.nl")
                .get("/myconext/api/eduid/eppn")
                .as(Map.class);
        List<String> values = (List<String>) map.get("eppn");
        assertEquals("1234567890@surfguest.nl", values.get(0));
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
        Map map = given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessTokenWithNoLinkedAccount("eduid.nl/eppn"))
                .get("/myconext/api/eduid/eppn")
                .as(Map.class);
        List<String> values = (List<String>) map.get("eppn");
        assertEquals(0, values.size());
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