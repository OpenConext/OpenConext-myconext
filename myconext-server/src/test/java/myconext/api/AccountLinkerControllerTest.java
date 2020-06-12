package myconext.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.LinkedAccount;
import myconext.model.MagicLinkRequest;
import myconext.model.User;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "mongodb_db=surf_id_test",
                "cron.node-cron-job-responsible=false",
                "email_guessing_sleep_millis=1",
                "sp_entity_id=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "sp_entity_metadata_url=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "spring.main.lazy-initialization=true",
                "oidc.base-url=http://localhost:8099/",
        })
public class AccountLinkerControllerTest extends AbstractIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8099);

    @Test
    public void linkAccountRedirect() throws IOException {
        String samlAuthnRequestId = "123456";

        String location = given().redirects().follow(false)
                .when()
                .contentType(ContentType.JSON)
                .get("/myconext/api/idp/oidc/account/" + samlAuthnRequestId)
                .getHeader("Location");
        assertEquals("http://localhost:8099/oidc/authorize?" +
                "scope=openid&" +
                "response_type=code&" +
                "redirect_uri=http://localhost:8081/myconext/api/idp/oidc/redirect&" +
                "state=123456&" +
                "client_id=myconext.rp.localhost", location);
    }

    @Test
    public void redirect() throws IOException {
        String eppn = "some@institute.nl";

        Map<Object, Object> body = new HashMap<>();
        body.put("eduperson_principal_name", eppn);
        body.put("schac_home_organization", "mock.idp");

        User user = doRedirect(body);
        LinkedAccount linkedAccount = user.getLinkedAccounts().get(0);
        assertEquals(eppn, linkedAccount.getEduPersonPrincipalName());
        assertEquals(eppn, linkedAccount.getInstitutionIdentifier(), "mock.idp");
        //second time the institution identifier is updated from the surf-crm-id
        body.put("surf-crm-id", "12345678");
        user = doRedirect(body);
        linkedAccount = user.getLinkedAccounts().get(0);

        assertEquals(linkedAccount.getInstitutionIdentifier(), "12345678");
        assertEquals("affiliate", linkedAccount.getEduPersonAffiliations().get(0));
    }

    @Test
    public void redirectWithAffiliations() throws IOException {
        String eppn = "some@institute.nl";

        Map<Object, Object> body = new HashMap<>();
        body.put("eduperson_principal_name", eppn);
        body.put("schac_home_organization", "mock.idp");
        body.put("eduperson_affiliation", Arrays.asList("student", "faculty"));

        User user = doRedirect(body);
        LinkedAccount linkedAccount = user.getLinkedAccounts().get(0);
        assertEquals(Arrays.asList("student", "faculty"), linkedAccount.getEduPersonAffiliations());
    }

    @Test
    public void redirectWithEmptyEppn() throws IOException {
        User user = doRedirect(Collections.emptyMap());
        assertEquals(0, user.getLinkedAccounts().size());
    }

    private User doRedirect(Map<Object, Object> userInfo) throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        //This ensures the user is tied to the authnRequest
        given().when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new MagicLinkRequest(authenticationRequestId, user("mdoe@example.com"), false, false))
                .put("/myconext/api/idp/magic_link_request")
                .then()
                .statusCode(HttpStatus.CREATED.value());
        stubForTokenUserInfo(userInfo);

        String location = given().redirects().follow(false)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", authenticationRequestId)
                .contentType(ContentType.JSON)
                .get("/myconext/api/idp/oidc/redirect")
                .getHeader("Location");
        assertTrue(location.startsWith("http://localhost:8081/saml/guest-idp/magic?h="));

        return userRepository.findOneUserByEmailIgnoreCase("mdoe@example.com");
    }

    @Test
    public void redirectExpiredAuthnRequest() throws IOException {
        String location = given().redirects().follow(false)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", "nope")
                .contentType(ContentType.JSON)
                .get("/myconext/api/idp/oidc/redirect")
                .getHeader("Location");
        assertEquals("http://localhost:3000/expired", location);
    }


    @Test
    public void spOidcLink() {
        User user = userRepository.findOneUserByEmailIgnoreCase("jdoe@example.com");
        Map res = given()
                .when()
                .get("/myconext/api/sp/oidc/link")
                .as(Map.class);
        assertEquals(res.get("url"),
                "http://localhost:8099/oidc/authorize?" +
                        "scope=openid&" +
                        "response_type=code&" +
                        "redirect_uri=http://localhost:8081/myconext/api/sp/oidc/redirect" +
                        "&state=" + UUID.nameUUIDFromBytes(user.getId().getBytes()).toString() +
                        "&prompt=login" +
                        "&client_id=myconext.rp.localhost");
    }

    @Test
    public void spFlowRedirect() throws IOException {
        User user = userRepository.findOneUserByEmailIgnoreCase("jdoe@example.com");
        String eppn = "some@institute.nl";

        Map<Object, Object> userInfo = new HashMap<>();
        userInfo.put("eduperson_principal_name", eppn);
        userInfo.put("schac_home_organization", "mock.idp");

        stubForTokenUserInfo(userInfo);

        String location = given().redirects().follow(false)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", UUID.nameUUIDFromBytes(user.getId().getBytes()).toString())
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/oidc/redirect")
                .getHeader("Location");
        assertEquals(location, "http://localhost:3001/institutions");

        user = userRepository.findOneUserByEmailIgnoreCase("jdoe@example.com");
        assertEquals(2, user.getLinkedAccounts().size());
    }

    private void stubForTokenUserInfo(Map<Object, Object> userInfo) throws JsonProcessingException {
        stubFor(post(urlPathMatching("/oidc/token")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(Collections.singletonMap("access_token", "123456")))));
        stubFor(post(urlPathMatching("/oidc/userinfo")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(userInfo))));
    }

}