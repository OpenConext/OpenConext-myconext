package myconext.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
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
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static myconext.api.AccountLinkerController.parseAffiliations;
import static org.junit.Assert.*;

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
        Response response = samlAuthnRequestResponseWithLoa(null, null, "");
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);
        //This ensures the user is tied to the authnRequest
        given().when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new MagicLinkRequest(authenticationRequestId, user("mdoe@example.com"), false, false))
                .put("/myconext/api/idp/magic_link_request")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        String location = given().redirects().follow(false)
                .when()
                .contentType(ContentType.JSON)
                .get("/myconext/api/idp/oidc/account/" + authenticationRequestId)
                .getHeader("Location");
        assertTrue(location.startsWith("http://localhost:8099/oidc/authorize?" +
                "scope=openid&" +
                "response_type=code&" +
                "redirect_uri=http://localhost:8081/myconext/api/idp/oidc/redirect&" +
                "state="));
    }

    @Test
    public void linkAccountRedirectWithExternalValidation() throws IOException {
        Response response = samlAuthnRequestResponseWithLoa(null, null, "");
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);
        //This ensures the user is tied to the authnRequest
        given().when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new MagicLinkRequest(authenticationRequestId, user("mdoe@example.com"), false, false))
                .put("/myconext/api/idp/magic_link_request")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        String location = given().redirects().follow(false)
                .when()
                .contentType(ContentType.JSON)
                .queryParam("useExternalValidation", true)
                .get("/myconext/api/idp/oidc/account/" + authenticationRequestId)
                .getHeader("Location");

        assertTrue(location.startsWith("http://localhost:8099/oidc/authorize?"));

        UriComponents uriComponent = UriComponentsBuilder.fromHttpUrl(location).build();
        MultiValueMap<String, String> queryParams = uriComponent.getQueryParams();
        assertEquals("openid", queryParams.getFirst("scope"));
        assertEquals("code", queryParams.getFirst("response_type"));
        assertEquals("http://localhost:8081/myconext/api/idp/oidc/redirect", queryParams.getFirst("redirect_uri"));
        assertEquals("http://mock-idp", queryParams.getFirst("login_hint"));
        assertEquals("https://manage.surfconext.nl/shibboleth", queryParams.getFirst("acr_values"));
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
        assertEquals("affiliate@mock.idp", linkedAccount.getEduPersonAffiliations().get(0));
    }

    @Test
    public void redirectWrongUser() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        given().when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new MagicLinkRequest(authenticationRequestId, user("mdoe@example.com"), false, false))
                .put("/myconext/api/idp/magic_link_request")
                .then()
                .statusCode(HttpStatus.CREATED.value());
        given().redirects().follow(false)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", String.format("id=%s&user_uid=%s", authenticationRequestId, "nope"))
                .contentType(ContentType.JSON)
                .get("/myconext/api/idp/oidc/redirect")
                .then()
                .statusCode(403);

    }

    @Test
    public void redirectWithValidateNames() throws IOException {
        Map<Object, Object> body = new HashMap<>();
        body.put("eduperson_principal_name", "some@institute.nl");
        body.put("schac_home_organization", "mock.idp");
        body.put("given_name", "Roger");
        body.put("family_name", "Johnson");

        String authnContext = readFile("request_authn_context_validated_name.xml");

        User user = doRedirect(body, authnContext, "http://localhost:8081/saml/guest-idp/magic?h");
        LinkedAccount linkedAccount = user.getLinkedAccounts().get(0);

        assertEquals("Roger", linkedAccount.getGivenName());
        assertEquals("Johnson", linkedAccount.getFamilyName());
    }

    @Test
    public void redirectWithNoEppn() throws IOException {
        Map<Object, Object> body = new HashMap<>();
        body.put("schac_home_organization", "mock.idp");
        body.put("given_name", "Roger");
        body.put("family_name", "Johnson");

        String authnContext = readFile("request_authn_context_validated_name.xml");

        User user = doRedirect(body, authnContext, "http://localhost:8081/saml/guest-idp/magic?h");
        LinkedAccount linkedAccount = user.getLinkedAccounts().get(0);

        assertNull(linkedAccount.getEduPersonPrincipalName());
    }

    @Test
    public void redirectWithoutValidateNames() throws IOException {
        Map<Object, Object> body = new HashMap<>();
        body.put("eduperson_principal_name", "some@institute.nl");
        body.put("schac_home_organization", "mock.idp");

        String authnContext = readFile("request_authn_context_validated_name.xml");

        doRedirect(body, authnContext, "http://localhost:3000/valid-name-missing/");
    }

    @Test
    public void redirectWithAffiliationStudent() throws IOException {
        Map<Object, Object> body = new HashMap<>();
        body.put("eduperson_principal_name", "some@institute.nl");
        body.put("schac_home_organization", "mock.idp");

        String authnContext = readFile("request_authn_context_affiliation_student.xml");

        doRedirect(body, authnContext, "http://localhost:3000/affiliation-missing");
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
        assertEquals(Stream.of("student@mock.idp", "faculty@mock.idp").sorted().collect(Collectors.toList()),
                linkedAccount.getEduPersonAffiliations().stream().sorted().collect(Collectors.toList()));
    }

    @Test
    public void redirectWithExistingEppn() throws IOException {
        User jdoe = userRepository.findOneUserByEmail("jdoe@example.com");
        String eppn = jdoe.getLinkedAccounts().get(0).getEduPersonPrincipalName();

        Map<Object, Object> userInfo = new HashMap<>();
        userInfo.put("eduperson_principal_name", eppn);
        userInfo.put("schac_home_organization", "mock.idp");

        String authenticationRequestId = samlAuthnRequest();
        User user = doRedirectResult(userInfo, authenticationRequestId,
                "http://localhost:3000/eppn-already-linked/");
        assertEquals(0, user.getLinkedAccounts().size());
    }

    @Test
    public void redirectWithEmptyEppn() throws IOException {
        User user = doRedirect(Collections.emptyMap());
        assertEquals(0, user.getLinkedAccounts().size());
    }

    private User doRedirect(Map<Object, Object> userInfo) throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        return doRedirectResult(userInfo, authenticationRequestId,
                "http://localhost:8081/saml/guest-idp/magic?h=");
    }

    private User doRedirect(Map<Object, Object> userInfo, String loaContext, String expectedLocation) throws IOException {
        Response response = samlAuthnRequestResponseWithLoa(null, null, loaContext);
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);
        return doRedirectResult(userInfo, authenticationRequestId, expectedLocation);
    }

    private User doRedirectResult(Map<Object, Object> userInfo, String authenticationRequestId, String expectedLocation) throws JsonProcessingException {
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
                .queryParam("state", stateParameterIdP(authenticationRequestId))
                .contentType(ContentType.JSON)
                .get("/myconext/api/idp/oidc/redirect")
                .getHeader("Location");
        assertTrue(location, location.startsWith(expectedLocation));

        return userRepository.findOneUserByEmail("mdoe@example.com");
    }

    @Test
    public void redirectExpiredAuthnRequest() throws IOException {
        String location = given().redirects().follow(false)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", stateParameterIdP("nope"))
                .contentType(ContentType.JSON)
                .get("/myconext/api/idp/oidc/redirect")
                .getHeader("Location");
        assertEquals("http://localhost:3000/expired", location);
    }

    @Test
    public void spOidcLink() {
        Map res = given()
                .when()
                .get("/myconext/api/sp/oidc/link")
                .as(Map.class);
        assertTrue(((String) res.get("url")).startsWith("http://localhost:8099/oidc/authorize?" +
                "scope=openid&" +
                "response_type=code&" +
                "redirect_uri=http://localhost:8081/myconext/api/sp/oidc/redirect"));
    }

    @Test
    public void spFlowRedirectWrongUser() throws IOException {
        given().redirects().follow(false)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", passwordEncoder.encode("nope"))
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/oidc/redirect")
                .then().statusCode(403);
    }

    @Test
    public void spFlowRedirect() throws IOException {
        String eppn = "some@institute.nl";

        Map<Object, Object> userInfo = new HashMap<>();
        userInfo.put("eduperson_principal_name", eppn);
        userInfo.put("schac_home_organization", "mock.idp");

        stubForTokenUserInfo(userInfo);

        String location = given().redirects().follow(false)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", passwordEncoder.encode("1234567890"))
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/oidc/redirect")
                .getHeader("Location");
        assertEquals(location, "http://localhost:3001/personal");

        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(3, user.getLinkedAccounts().size());
    }

    @Test
    public void parseUserAffiliations() {
        //No schachome
        assertEquals(0, parseAffiliations(new HashMap<>(), "").size());
        //No affiliations
        assertEquals(Collections.singletonList("affiliate@example.com"), parseAffiliations(new HashMap<>(), "example.com"));
        //Duplicate affiliations
        Map<String, Object> idpAttributes = new HashMap<>();
        idpAttributes.put("eduperson_affiliation", Collections.singletonList("student"));
        idpAttributes.put("eduperson_scoped_affiliation", Collections.singletonList("student@example.com"));
        assertEquals(Collections.singletonList("student@example.com"), parseAffiliations(idpAttributes, "example.com"));
        ///Added affiliations
        idpAttributes = new HashMap<>();
        idpAttributes.put("eduperson_affiliation", Arrays.asList("student", "some"));
        idpAttributes.put("eduperson_scoped_affiliation", Collections.singletonList("value@example.com"));
        assertEquals(Stream.of("some@example.com", "student@example.com", "value@example.com").sorted().collect(Collectors.toList()),
                parseAffiliations(idpAttributes, "example.com").stream().sorted().collect(Collectors.toList()));
    }


    private void stubForTokenUserInfo(Map<Object, Object> userInfo) throws JsonProcessingException {
        stubFor(post(urlPathMatching("/oidc/token")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(Collections.singletonMap("access_token", "123456")))));
        stubFor(post(urlPathMatching("/oidc/userinfo")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(userInfo))));
    }

    private String stateParameterSp() {
        return passwordEncoder.encode("mdoe");
    }

    private String stateParameterIdP(String authenticationRequestId) {
        return String.format("id=%s&user_uid=%s", authenticationRequestId, stateParameterSp());
    }
}