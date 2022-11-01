package myconext.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import myconext.AbstractIntegrationTest;
import myconext.model.*;
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
    public void linkAccountRedirectExpired() throws IOException {
        String location = given().redirects().follow(false)
                .when()
                .contentType(ContentType.JSON)
                .get("/myconext/api/idp/oidc/account/nope")
                .getHeader("Location");
        assertTrue(location.endsWith("expired"));
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

        assertEquals("12345678", linkedAccount.getInstitutionIdentifier());
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
        assertEquals("http://localhost:3001/personal", location);

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

    @Test
    public void createFromInstitution() {
        Map<String, String> results = given().redirects().follow(false)
                .when()
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/create-from-institution")
                .as(new TypeRef<>() {
                });

        assertTrue(results.get("url").startsWith("http://localhost:8099/oidc/authorize?scope=openid&response_type=code&redirect_uri=http://localhost:8081/myconext/api/sp/create-from-institution/oidc-redirect&state="));
    }

    @Test
    public void spCreateFromInstitutionRedirectNoSession() {
        given().redirects().follow(false)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", "state")
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/create-from-institution/oidc-redirect")
                .then()
                .statusCode(403);
    }

    @Test
    public void spCreateFromInstitutionRedirectNoSessionWrongState() {
        CookieFilter cookieFilter = new CookieFilter();
        given().redirects().follow(false)
                .filter(cookieFilter)
                .when()
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/create-from-institution")
                .then()
                .statusCode(200);
        given().redirects().follow(false)
                .filter(cookieFilter)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", "state")
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/create-from-institution/oidc-redirect")
                .then()
                .statusCode(403);
    }

    @Test
    public void spCreateFromInstitutionRedirectEppnExists() throws JsonProcessingException {
        CookieFilter cookieFilter = new CookieFilter();
        Map<String, String> results = given()
                .filter(cookieFilter)
                .when()
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/create-from-institution")
                .as(new TypeRef<>() {
                });
        String state = UriComponentsBuilder.fromUriString(results.get("url")).build().getQueryParams().getFirst("state");

        Map<Object, Object> userInfo = Map.of(
                "eduperson_principal_name", "1234567890@surfguest.nl"
        );

        stubForTokenUserInfo(userInfo);

        String location = given().redirects().follow(false)
                .filter(cookieFilter)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", state)
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/create-from-institution/oidc-redirect")
                .getHeader("Location");
        assertEquals("http://localhost:3001/create-from-institution/eppn-already-linked?fromInstitution=true&email=jdoe%40example.com",
                location);
    }

    @Test
    public void spCreateFromInstitutionRedirectWrongHash() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);

        given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("hash", hash + "bogus")
                .get("/myconext/api/sp/create-from-institution/info")
                .then()
                .statusCode(403);
    }

    @Test
    public void spCreateFromInstitutionRedirectInfo() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);

        Map<String, String> info = given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("hash", hash)
                .get("/myconext/api/sp/create-from-institution/info")
                .as(new TypeRef<>() {
                });
        assertEquals(userInfo, info);
    }

    @Test
    public void spCreateFromInstitutionPollWrongHash() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);

        given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("hash", hash + "bogus")
                .get("/myconext/api/sp/create-from-institution/poll")
                .then()
                .statusCode(403);
    }

    @Test
    public void spCreateFromInstitutionPoll() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);

        Integer loginStatus = given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("hash", hash)
                .get("/myconext/api/sp/create-from-institution/poll")
                .as(new TypeRef<>() {
                });
        assertEquals(LoginStatus.NOT_LOGGED_IN, Arrays.asList(LoginStatus.values()).get(loginStatus));
    }

    @Test
    public void spCreateFromInstitutionResendMail() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);
        //We can only resend, if the first mail is send
        CreateInstitutionEduID createInstitutionEduID = new CreateInstitutionEduID(hash, "new@example.com", true);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(createInstitutionEduID)
                .post("/myconext/api/sp/create-from-institution/email")
                .then()
                .statusCode(200);

        given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("hash", hash)
                .get("/myconext/api/sp/create-from-institution/resendMail")
                .then()
                .statusCode(200);
    }

    @Test
    public void spCreateFromInstitutionResendMailWrongStatus() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);

        swapStatus(hash, LoginStatus.LOGGED_IN_SAME_DEVICE);
        given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("hash", hash)
                .get("/myconext/api/sp/create-from-institution/resendMail")
                .then()
                .statusCode(403);
        swapStatus(hash, LoginStatus.NOT_LOGGED_IN);
    }

    @Test
    public void spCreateFromInstitutionResendEmailWrongHash() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);

        given()
                .when()
                .contentType(ContentType.JSON)
                .queryParam("hash", hash + "bogus")
                .get("/myconext/api/sp/create-from-institution/resendMail")
                .then()
                .statusCode(403);
    }

    @Test
    public void spCreateFromInstitutionLinkFromInstitutionDuplicateEmail() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);
        CreateInstitutionEduID createInstitutionEduID = new CreateInstitutionEduID(hash, "jdoe@example.com", true);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(createInstitutionEduID)
                .post("/myconext/api/sp/create-from-institution/email")
                .then()
                .statusCode(409);
    }

    @Test
    public void spCreateFromInstitutionLinkFromInstitutionUserNotFound() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);
        CreateInstitutionEduID createInstitutionEduID = new CreateInstitutionEduID(hash, "new@example.com", false);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(createInstitutionEduID)
                .post("/myconext/api/sp/create-from-institution/email")
                .then()
                .statusCode(404);
    }

    @Test
    public void spCreateFromInstitutionLinkFromInstitutionNewUser() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);
        CreateInstitutionEduID createInstitutionEduID = new CreateInstitutionEduID(hash, "new@example.com", true);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(createInstitutionEduID)
                .post("/myconext/api/sp/create-from-institution/email")
                .then()
                .statusCode(200);

        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByHash(hash).get();
        assertEquals("new@example.com", requestInstitutionEduID.getCreateInstitutionEduID().getEmail());
    }

    @Test
    public void spCreateFromInstitutionLinkFromInstitutionExistingUser() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);
        CreateInstitutionEduID createInstitutionEduID = new CreateInstitutionEduID(hash, "jdoe@example.com", false);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(createInstitutionEduID)
                .post("/myconext/api/sp/create-from-institution/email")
                .then()
                .statusCode(200);

        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByHash(hash).get();
        assertEquals("jdoe@example.com", requestInstitutionEduID.getCreateInstitutionEduID().getEmail());
    }

    @Test
    public void spCreateFromInstitutionLinkFromInstitutionWrongHash() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);
        CreateInstitutionEduID createInstitutionEduID = new CreateInstitutionEduID(hash + "bogus", "jdoe@example.com", false);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(createInstitutionEduID)
                .post("/myconext/api/sp/create-from-institution/email")
                .then()
                .statusCode(403);
    }

    @Test
    public void spCreateFromInstitutionLinkFromInstitutionFinishNewUser() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);
        CreateInstitutionEduID createInstitutionEduID = new CreateInstitutionEduID(hash, "new@example.com", true);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(createInstitutionEduID)
                .post("/myconext/api/sp/create-from-institution/email")
                .then()
                .statusCode(200);

        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByHash(hash).get();
        String emailHash = requestInstitutionEduID.getEmailHash();
        String location = given().redirects().follow(false)
                .when()
                .contentType(ContentType.JSON)
                .queryParam("h", emailHash)
                .get("/myconext/api/sp/create-from-institution/finish")
                .getHeader("Location");
        User user = userRepository.findUserByEmail("new@example.com").get();
        assertEquals("http://localhost:3000/create-from-institution-login?key=" + user.getCreateFromInstitutionKey(), location);
    }

    @Test
    public void spCreateFromInstitutionLinkFromInstitutionFinishExistingUser() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);
        CreateInstitutionEduID createInstitutionEduID = new CreateInstitutionEduID(hash, "jdoe@example.com", false);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(createInstitutionEduID)
                .post("/myconext/api/sp/create-from-institution/email")
                .then()
                .statusCode(200);

        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByHash(hash).get();
        String emailHash = requestInstitutionEduID.getEmailHash();
        String location = given().redirects().follow(false)
                .when()
                .contentType(ContentType.JSON)
                .queryParam("h", emailHash)
                .get("/myconext/api/sp/create-from-institution/finish")
                .getHeader("Location");
        User user = userRepository.findUserByEmail("jdoe@example.com").get();
        assertEquals("http://localhost:3000/create-from-institution-login?key=" + user.getCreateFromInstitutionKey(), location);
    }

    @Test
    public void spCreateFromInstitutionLinkFromInstitutionFinishUserNotFound() throws JsonProcessingException {
        Map<Object, Object> userInfo = userInfoMap("new-user@qwerty.com");
        String hash = getHashFromCreateInstitutionFlow(userInfo);
        CreateInstitutionEduID createInstitutionEduID = new CreateInstitutionEduID(hash, "jdoe@example.com", false);
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(createInstitutionEduID)
                .post("/myconext/api/sp/create-from-institution/email")
                .then()
                .statusCode(200);

        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByHash(hash).get();
        requestInstitutionEduID.setUserId("bogus");
        requestInstitutionEduIDRepository.save(requestInstitutionEduID);
        String emailHash = requestInstitutionEduID.getEmailHash();
        given().redirects().follow(false)
                .when()
                .contentType(ContentType.JSON)
                .queryParam("h", emailHash)
                .get("/myconext/api/sp/create-from-institution/finish")
                .then()
                .statusCode(404);
    }

    private Map<Object, Object> userInfoMap(String eppn) {
        return Map.of(
                "eduperson_principal_name", eppn,
                "given_name", "Toto",
                "family_name", "Soo",
                "schac_home_organization", "qwerty.com"
        );
    }

    private String getHashFromCreateInstitutionFlow(Map<Object, Object> userInfo) throws JsonProcessingException {
        CookieFilter cookieFilter = new CookieFilter();
        Map<String, String> results = given()
                .filter(cookieFilter)
                .when()
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/create-from-institution")
                .as(new TypeRef<>() {
                });
        String state = UriComponentsBuilder.fromUriString(results.get("url")).build().getQueryParams().getFirst("state");

        stubForTokenUserInfo(userInfo);

        String location = given().redirects().follow(false)
                .filter(cookieFilter)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", state)
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/create-from-institution/oidc-redirect")
                .getHeader("Location");
        assertTrue(location.startsWith("http://localhost:3001/create-from-institution/link/"));

        return location.substring(location.indexOf("link/") + "link/".length());
    }

    private void swapStatus(String hash, LoginStatus newStatus) {
        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByHash(hash).get();
        requestInstitutionEduID.setLoginStatus(newStatus);
        requestInstitutionEduIDRepository.save(requestInstitutionEduID);
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