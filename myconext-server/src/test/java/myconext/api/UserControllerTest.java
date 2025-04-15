package myconext.api;

import com.yubico.webauthn.data.*;
import com.yubico.webauthn.data.exception.Base64UrlException;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import myconext.AbstractIntegrationTest;
import myconext.model.*;
import myconext.repository.ChallengeRepository;
import myconext.security.ACR;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.CookieStore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static myconext.model.LinkedAccountTest.linkedAccount;
import static myconext.security.GuestIdpAuthenticationRequestFilter.BROWSER_SESSION_COOKIE_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

@SuppressWarnings("unchecked")
public class UserControllerTest extends AbstractIntegrationTest {

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private ChallengeRepository challengeRepository;

    @Test
    public void existingUser() throws IOException {
        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(user("jdoe@example.com"), HttpMethod.PUT);

        when()
                .get("/myconext/api/idp/service/name/" + magicLinkResponse.authenticationRequestId)
                .then()
                .body("name", equalTo("Bart TEst spd 6.00"));

        magicLinkResponse.response
                .statusCode(HttpStatus.CREATED.value());
        String samlResponse = samlResponse(magicLinkResponse);
        assertTrue(samlResponse.contains("jdoe@example.com"));
        assertTrue(samlResponse.contains("urn:oasis:names:tc:SAML:attribute:subject-id"));

        when()
                .get("/myconext/api/idp/resend_code_request?id=" + magicLinkResponse.authenticationRequestId)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void serviceNameDummy() throws IOException {
        when()
                .get("/myconext/api/idp/service/name/42")
                .then()
                .body("name", equalTo("This Beautiful Service"));
    }

    @Test
    public void newUserNotFound() throws IOException {
        oneTimeLoginCodeRequest(user("new@example.com"), HttpMethod.PUT)
                .response
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void duplicateEmailNewUser() throws IOException {
        oneTimeLoginCodeRequest(user("jdoe@example.com"), HttpMethod.POST)
                .response
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void newUserProvisioned() throws IOException {
        User user = user("new@example.com", "Mary", "Doe", "en");

        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(user, HttpMethod.POST);
        User userFromDB = userRepository.findUserByEmailAndRateLimitedFalse(user.getEmail()).get();
        assertEquals(0L, ReflectionTestUtils.getField(userFromDB, "lastSeenAppNudge"));
        assertEquals(user.getGivenName(), userFromDB.getGivenName());

        String samlResponse = samlResponse(magicLinkResponse);
        assertTrue(samlResponse.contains("new@example.com"));

        User userFromAfter = userRepository.findUserByEmailAndRateLimitedFalse(user.getEmail()).get();
        assertEquals(0L, ReflectionTestUtils.getField(userFromAfter, "lastSeenAppNudge"));

        when()
                .get("/myconext/api/idp/resend_code_request?id=" + magicLinkResponse.authenticationRequestId)
                .then()
                .statusCode(HttpStatus.OK.value());

    }

    @Test
    public void authenticationRequestExpired() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        doExpireWithFindProperty(SamlAuthenticationRequest.class, "id", authenticationRequestId);

        ClientAuthenticationRequest linkRequest = new ClientAuthenticationRequest(authenticationRequestId,
                user("new@example.com", "Mary", "Doe", "en"), false);

        oneTimeLoginCodeRequest(linkRequest, HttpMethod.POST)
                .response
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }


    @Test
    public void accountLinkingRequiredNotNeeded() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        LinkedAccount linkedAccount = linkedAccount("John", "Doe", new Date());
        user.getLinkedAccounts().add(linkedAccount);
        userRepository.save(user);

        String authnContext = readFile("request_authn_context.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);

        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        String samlResponse = samlResponse(magicLinkResponse);

        assertTrue(samlResponse.contains(ACR.LINKED_INSTITUTION));
    }

    @Test
    public void accountLinkingWithValidatedNames() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");

        String authnContext = readFile("request_authn_context_validated_name.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);

        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        String samlResponse = samlResponse(magicLinkResponse);

        assertTrue(samlResponse.contains(ACR.VALIDATE_NAMES));
        assertTrue(samlResponse.contains(user.getDerivedGivenName()));
        assertTrue(samlResponse.contains(user.getDerivedFamilyName()));
    }

    @Test
    public void accountLinkingWithoutStudentAffiliation() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        Date createdAt = Date.from(Instant.now().plus(30 * 365, ChronoUnit.DAYS));
        LinkedAccount linkedAccount = linkedAccount(createdAt, Arrays.asList("nope"));
        user.getLinkedAccounts().add(linkedAccount);
        userRepository.save(user);

        String authnContext = readFile("request_authn_context_affiliation_student.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);

        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId).get();
        //fake that we want to proceed
        samlAuthenticationRequest.setSteppedUp(StepUpStatus.FINISHED_STEP_UP);
        authenticationRequestRepository.save(samlAuthenticationRequest);

        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        String samlResponse = this.samlResponse(magicLinkResponse);

        assertTrue(samlResponse.contains("Your institution has not provided this affiliation"));
        assertTrue(samlResponse.contains("urn:oasis:names:tc:SAML:2.0:status:Responder"));
        assertTrue(samlResponse.contains("urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext"));
        assertFalse(samlResponse.contains("Assertion"));
        assertFalse(samlResponse.contains(ACR.AFFILIATION_STUDENT));
    }

    @Test
    public void accountLinkingWithoutValidNames() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.getLinkedAccounts().clear();
        LinkedAccount linkedAccount = linkedAccount("", "", new Date());
        user.getLinkedAccounts().add(linkedAccount);
        userRepository.save(user);

        String authnContext = readFile("request_authn_context_validated_name.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);

        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId).get();
        //fake that we want to proceed
        samlAuthenticationRequest.setSteppedUp(StepUpStatus.FINISHED_STEP_UP);
        authenticationRequestRepository.save(samlAuthenticationRequest);

        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        String samlResponse = this.samlResponse(magicLinkResponse);

        assertTrue(samlResponse.contains("Your institution has not provided those attributes"));
        assertTrue(samlResponse.contains("urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext"));
    }

    @Test
    public void accountLinkingWithoutValidExternalNames() throws IOException {
        String authnContext = readFile("request_authn_context_validated_external_name.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);

        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId).get();
        //fake that we want to proceed
        samlAuthenticationRequest.setSteppedUp(StepUpStatus.FINISHED_STEP_UP);
        authenticationRequestRepository.save(samlAuthenticationRequest);

        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        String samlResponse = this.samlResponse(magicLinkResponse);

        assertTrue(samlResponse.contains("Your identity is not verified by an external trusted party"));
        assertTrue(samlResponse.contains("urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext"));
    }

    @Test
    public void accountLinkingWithValidatedNamesByExternalLinkedAccount() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.getLinkedAccounts().clear();
        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount(
                UUID.randomUUID().toString(),
                IdpScoping.studielink,
                new VerifyIssuer(IdpScoping.studielink.name(), IdpScoping.studielink.name(), null),
                Verification.Geverifieerd,
                UUID.randomUUID().toString(),
                IdpScoping.studielink.name(),
                IdpScoping.studielink.name(),
                null,
                null,
                "Johny",
                "John",
                "Doe",
                "Doe",
                null,
                null,
                null,
                null,
                new Date(),
                new Date(),
                Date.from(Instant.now().plus(365 * 5, ChronoUnit.DAYS)),
                true
        );
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        userRepository.save(user);

        String authnContext = readFile("request_authn_context_validated_name.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);

        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        String samlResponse = samlResponse(magicLinkResponse);

        assertTrue(samlResponse.contains(ACR.VALIDATE_NAMES));
        assertTrue(samlResponse.contains(user.getFamilyName()));
        assertTrue(samlResponse.contains(user.getGivenName()));
    }

    @Test
    public void invalidLoaLevel() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        String authnContext = readFile("request_authn_context_invalid.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);

        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);
        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        String samlResponse = this.samlResponse(magicLinkResponse);

        assertTrue(samlResponse.contains("The specified authentication context requirements"));
        assertTrue(samlResponse.contains("cannot be met by the responder"));
        assertTrue(samlResponse.contains("urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext"));
    }

    @Test
    public void institutionalDomainNames() {
        given()
                .when()
                .get("/myconext/api/idp/email/domain/institutional")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(17));
    }

    @Test
    public void allowedDomainNames() {
        given()
                .when()
                .get("/myconext/api/idp/email/domain/allowed")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("size()", is(0));
    }

    @Test
    public void emptySamlRequest() {
        given().redirects().follow(false)
                .when()
                .queryParams(Collections.emptyMap())
                .get("/saml/guest-idp/SSO")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void relayState() throws IOException {
        User user = user("steve@example.com", "Steve", "Doe", "en");
        String authenticationRequestId = samlAuthnRequest("Nice");
        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.POST);
        Response response = magicResponse(magicLinkResponse);
        assertTrue(IOUtils.toString(response.asInputStream(), Charset.defaultCharset()).contains("Nice"));
    }

    @Test
    public void updateUser() {
        UpdateUserNameRequest updateUserNameRequest = new UpdateUserNameRequest("chosenName", "Mary", "Poppins");
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(updateUserNameRequest)
                .put("/myconext/api/sp/update")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");
        //Has linked accounts, so no update
        assertEquals(userFromDB.getGivenName(), "Mary");
        assertEquals(userFromDB.getFamilyName(), "Poppins");
        assertEquals(userFromDB.getChosenName(), updateUserNameRequest.getChosenName());
    }

    @Test
    public void removeUserLinkedAccounts() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(2, user.getLinkedAccounts().size());
        LinkedAccount linkedAccount = user.getLinkedAccounts().get(0);
        UpdateLinkedAccountRequest updateLinkedAccountRequest = new UpdateLinkedAccountRequest(linkedAccount.getEduPersonPrincipalName(), null, false, null, "nope");
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(updateLinkedAccountRequest)
                .put("/myconext/api/sp/institution")
                .then()
                .statusCode(HttpStatus.OK.value());

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");

        assertEquals(1, userFromDB.getLinkedAccounts().size());
    }

    @Test
    public void removeUserExternalLinkedAccount() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount(
                "subjectID", IdpScoping.idin, true
        );
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        userRepository.save(user);

        UpdateLinkedAccountRequest updateLinkedAccountRequest = new UpdateLinkedAccountRequest(
                null, externalLinkedAccount.getSubjectId(), true, IdpScoping.idin.name(), null);
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(updateLinkedAccountRequest)
                .put("/myconext/api/sp/institution")
                .then()
                .statusCode(HttpStatus.OK.value());

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");

        assertEquals(0, userFromDB.getExternalLinkedAccounts().size());
    }

    @Test
    public void updatePublicKeyCredential() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        PublicKeyCredentials publicKeyCredentials = user.getPublicKeyCredentials().get(0);
        Map<String, String> body = new HashMap<>();
        body.put("identifier", publicKeyCredentials.getIdentifier());
        body.put("name", "Red ubi-key");
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .post("/myconext/api/sp/credential")
                .then()
                .statusCode(HttpStatus.OK.value());

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");

        assertEquals(body.get("name"), userFromDB.getPublicKeyCredentials().get(0).getName());
    }

    @Test
    public void updatePublicKeyCredentialNotFound() {
        Map<String, String> body = new HashMap<>();
        body.put("identifier", "nope");
        body.put("name", "Red ubi-key");
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .post("/myconext/api/sp/credential")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void removePublicKeyCredential() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        PublicKeyCredentials publicKeyCredentials = user.getPublicKeyCredentials().get(0);
        Map<String, String> body = Collections.singletonMap("identifier", publicKeyCredentials.getIdentifier());
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .put("/myconext/api/sp/credential")
                .then()
                .statusCode(HttpStatus.OK.value());

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");

        assertEquals(0, userFromDB.getPublicKeyCredentials().size());
    }

    @Test
    public void removeUserService() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        String entityID = "http://mock-sp";

        assertEquals(2, user.getEduIDS().size());
        assertTrue(user.getEduIDS().stream()
                .anyMatch(val -> val.getServices().stream().anyMatch(service -> entityID.equals(service.getEntityId()))));

        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new DeleteService(entityID, new ArrayList<>()))
                .put("/myconext/api/sp/service")
                .then()
                .statusCode(HttpStatus.OK.value());

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");

        assertFalse(userFromDB.getEduIDS().stream()
                .anyMatch(val -> val.getServices().stream().anyMatch(service -> entityID.equals(service.getEntityId()))));
        assertEquals(1, userFromDB.getEduIDS().size());
    }

    @Test
    public void updateUserWeakPassword() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        passwordResetHashRepository.save(new PasswordResetHash(user, "hash"));
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new UpdateUserSecurityRequest("nope", "hash"))
                .put("/myconext/api/sp/update-password")
                .then()
                .statusCode(422);
    }

    @Test
    public void resetPasswordHashValid() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        passwordResetHashRepository.save(new PasswordResetHash(user, "hash"));
        Boolean valid = given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .param("hash", "hash")
                .get("/myconext/api/sp/password-reset-hash-valid")
                .as(Boolean.class);
        assertTrue(valid);
    }

    @Test
    public void resetPasswordHashInvalid() {
        Boolean valid = given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .param("hash", "hash")
                .get("/myconext/api/sp/password-reset-hash-valid")
                .as(Boolean.class);
        assertFalse(valid);
    }

    @Test
    public void updateUserPassword() {
        SecureRandom random = new SecureRandom();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        passwordResetHashRepository.save(new PasswordResetHash(user, "hash"));
        UpdateUserSecurityRequest updateUserSecurityRequest = new UpdateUserSecurityRequest("correctSecret001", "hash");
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(updateUserSecurityRequest)
                .put("/myconext/api/sp/update-password")
                .then()
                .statusCode(HttpStatus.CREATED.value());
        user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertTrue(passwordEncoder.matches(updateUserSecurityRequest.getNewPassword(), user.getPassword()));
    }

    @Test
    public void updateUserPasswordWrongHash() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        UpdateUserSecurityRequest updateUserSecurityRequest = new UpdateUserSecurityRequest("correctSecret001", null);
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(updateUserSecurityRequest)
                .put("/myconext/api/sp/update-password")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void updateUserAfterPasswordForgotten() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.setForgottenPassword(true);
        ReflectionTestUtils.setField(user, "password", "abcdefghijklmnop");
        userRepository.save(user);

        passwordResetHashRepository.save(new PasswordResetHash(user, "hash"));

        UpdateUserSecurityRequest securityRequest = new UpdateUserSecurityRequest("abcdefghujklmnop", "hash");
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(securityRequest)
                .put("/myconext/api/sp/update-password")
                .then()
                .statusCode(HttpStatus.CREATED.value());
        user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertTrue(passwordEncoder.matches(securityRequest.getNewPassword(), user.getPassword()));
    }

    @Test
    public void updateUserAfterPasswordForgottenNoHash() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.setForgottenPassword(true);
        ReflectionTestUtils.setField(user, "password", "abcdefghijklmnop");
        userRepository.save(user);

        UpdateUserSecurityRequest securityRequest = new UpdateUserSecurityRequest("abcdefghujklmnop", "hash");
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(securityRequest)
                .put("/myconext/api/sp/update-password")
                .then()
                .statusCode(403);
    }

    @Test
    public void deleteUserPassword() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        passwordResetHashRepository.save(new PasswordResetHash(user, "hash"));
        UpdateUserSecurityRequest updateUserSecurityRequest = new UpdateUserSecurityRequest(null, "hash");
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(updateUserSecurityRequest)
                .put("/myconext/api/sp/update-password")
                .then()
                .statusCode(HttpStatus.CREATED.value());
        user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertNull(user.getPassword());
    }

    @Test
    public void deleteUser() {
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .delete("/myconext/api/sp/delete")
                .then()
                .statusCode(HttpStatus.OK.value())
                .cookie("SESSION", "");

        Optional<User> optionalUser = userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com");
        assertFalse(optionalUser.isPresent());
    }

    @Test
    public void logout() {
        Map res = given()
                .when()
                .get("/myconext/api/sp/logout")
                .as(Map.class);
        assertEquals(200, res.get("status"));
    }

    @Test
    public void personal() {
        Map res = given()
                .when()
                .get("/myconext/api/sp/personal")
                .as(Map.class);
        assertEquals("jdoe@example.com", res.get("email"));
        assertFalse(res.containsKey("password"));
        assertFalse(res.containsKey("surfSecureId"));
    }

    @Test
    public void forgetMe() {
        String s = given()
                .when()
                .delete("/myconext/api/sp/forget")
                .getBody().asString();
        assertEquals("0", s);
    }

    @Test
    public void loginWithPassword() throws IOException {
        User user = user("mdoe@example.com");
        userSetPassword(user, "Secret123");
        String authenticationRequestId = samlAuthnRequest();
        ClientAuthenticationRequest magicLinkRequest = new ClientAuthenticationRequest(authenticationRequestId, user, true);

        CookieFilter cookieFilter = new CookieFilter();
        Response response = given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(magicLinkRequest)
                .filter(cookieFilter)
                .put("/myconext/api/idp/generate_code_request");

        String url = (String) response.body().as(Map.class).get("url");
        url = url.replace("8081", this.port + "");

        response = given()
                .redirects().follow(false)
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .filter(cookieFilter)
                .get(url);
        String html = samlAuthnResponse(response, Optional.of(cookieFilter));
        assertTrue(html.contains("mdoe@example.com"));

        //now that we are logged in we can use the JSESSION COOKIE to test SSO
        CookieStore cookieStore = (CookieStore) ReflectionTestUtils.getField(cookieFilter, "cookieStore");
        org.apache.http.cookie.Cookie cookie = cookieStore.getCookies()
                .stream()
                .filter(c -> c.getName().equalsIgnoreCase("session"))
                .findFirst()
                .get();
        response = samlAuthnRequestResponse(new Cookie.Builder(cookie.getName(), cookie.getValue()).build(), "relay");
        html = samlAuthnResponse(response, Optional.empty());
        assertTrue(html.contains("mdoe@example.com"));
        assertFalse(html.contains("null"));
    }

    @Test
    public void loginWithWrongPassword() throws IOException {
        User user = user("mdoe@example.com");
        userSetPassword(user, "nope");
        String authenticationRequestId = samlAuthnRequest();
        ClientAuthenticationRequest magicLinkRequest = new ClientAuthenticationRequest(authenticationRequestId, user, true);

        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(magicLinkRequest)
                .put("/myconext/api/idp/generate_code_request")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void magicLinkCanNotBeReused() throws IOException {
        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(user("jdoe@example.com"), HttpMethod.PUT);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();
        String samlResponse = samlResponse(magicLinkResponse);
        assertTrue(samlResponse.contains("jdoe@example.com"));

        given().redirects().follow(false)
                .when()
                .queryParam("h", samlAuthenticationRequest.getHash())
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .get("/saml/guest-idp/magic")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", "http://localhost:3000/expired");
    }

    @Test
    public void prefetch() throws IOException {
        Response response = given().redirects().follow(false)
                .when()
                .queryParam("h", " ")
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .get("/saml/guest-idp/magic");

        String uri = response.getHeader("Location");
        assertTrue(uri.contains("expired"));
    }

    @Test
    public void stepup() throws IOException {
        User jdoe = userRepository.findOneUserByEmail("jdoe@example.com");
        jdoe.getLinkedAccounts().clear();
        userRepository.save(jdoe);

        String authnContext = readFile("request_authn_context.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, null, authnContext);
        assertEquals(302, response.getStatusCode());

        String location = response.getHeader("Location");
        assertTrue(location.startsWith("http://localhost:3000/login/"));

        String authenticationRequestId = location.substring(location.lastIndexOf("/") + 1, location.lastIndexOf("?"));
        User user = user("jdoe@example.com");
        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, StringUtils.hasText(user.getPassword())), HttpMethod.PUT);

        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();
        response = given().redirects().follow(false)
                .when()
                .queryParam("h", samlAuthenticationRequest.getHash())
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .get("/saml/guest-idp/magic");

        //stepup screen
        String uri = response.getHeader("Location");
        assertTrue(uri.contains("stepup"));

        response = given().redirects().follow(false)
                .when()
                .get("/myconext/api/idp/oidc/account/" + samlAuthenticationRequest.getId());
        location = response.getHeader("Location");
        assertTrue(location.startsWith("http://localhost:8098/oidc/authorize?" +
                "scope=openid&response_type=code&" +
                "redirect_uri=http://localhost:8081/myconext/api/idp/oidc/redirect&" +
                "state="));
    }

    @Test
    public void mfa() throws IOException {
        String authnContext = readFile("request_authn_context_mfa.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, null, authnContext);
        assertEquals(302, response.getStatusCode());

        String location = response.getHeader("Location");
        assertTrue(location.startsWith("http://localhost:3000/login/"));

        String authenticationRequestId = location.substring(location.lastIndexOf("/") + 1, location.lastIndexOf("?"));
        User user = user("jdoe@example.com");
        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, StringUtils.hasText(user.getPassword())), HttpMethod.PUT);

        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();
        response = given().redirects().follow(false)
                .when()
                .queryParam("h", samlAuthenticationRequest.getHash())
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .get("/saml/guest-idp/magic");

        //stepup screen
        String uri = response.getHeader("Location");
        assertTrue(uri.contains("app-required"));
    }

    @Test
    public void mfaNoTiqrAuthentication() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        String authnContext = readFile("request_authn_context_mfa.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);
        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();

        Response magicResponse = given().redirects().follow(false)
                .when()
                .queryParam("h", samlAuthenticationRequest.getHash())
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .get("/saml/guest-idp/magic");
        while (magicResponse.statusCode() == 302) {
            String location = magicResponse.getHeader("Location");
            assertNotNull(location);
            magicResponse = this.get302Response(magicResponse, Optional.empty(), "?force=true");
        }
        String samlResponse = samlAuthnResponse(magicResponse, Optional.empty());

        assertTrue(samlResponse.contains("urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext"));
        assertTrue(samlResponse.contains("The requesting service has indicated that a login with the eduID app is required to login"));
    }

    @Test
    public void mfaTiqrAuthenticated() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        String authnContext = readFile("request_authn_context_mfa.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);

        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();
        samlAuthenticationRequest.setTiqrFlow(true);
        authenticationRequestRepository.save(samlAuthenticationRequest);

        String samlResponse = samlResponse(magicLinkResponse);
        assertTrue(samlResponse.contains(ACR.PROFILE_MFA));
    }

    @Test
    public void stepUpValidateName() throws IOException {
        User jdoe = userRepository.findOneUserByEmail("jdoe@example.com");
        jdoe.getLinkedAccounts().clear();
        userRepository.save(jdoe);

        String authnContext = readFile("request_authn_context_validated_name.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, null, authnContext);

        String location = response.getHeader("Location");
        String authenticationRequestId = location.substring(location.lastIndexOf("/") + 1, location.lastIndexOf("?"));

        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId).get();
        assertTrue(samlAuthenticationRequest.getAuthenticationContextClassReferences().contains(ACR.VALIDATE_NAMES));
    }

    @Test
    public void webAuhthRegistration() throws Base64UrlException, IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(1, user.getPublicKeyCredentials().size());

        String token = given().when().get("/myconext/api/sp/security/webauthn")
                .then().extract().body().jsonPath().getString("token");
        String attestation = "o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YVj_SZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2NFXtS5-K3OAAI1vMYKZIsLJfHwVQMAewFBb2GSzftwFaO2t01mWWDmvgjYzJfp9YL55miRnO9cO4jnIIyPjPgXY7XoJCXHf70tN9cMrM8haGb8D1TfpS-1ijer4ChFgDypfhNGUfAxbfs7mk-IQR3gS8Afxfks17mE68fid_4lZt7lOi6Z9-RiWx_m6BbX0OjViaUBAgMmIAEhWCAyic9YLBoxd3zvxePnDbBktATOgR5jBvrUFdPptlyg-SJYIFv4tjumhccPrGTFkUBvtbQKhrDZcGWdIBhFlF_JtGIR";

        Map res = given()
                .when()
                .body(Collections.singletonMap("token", token))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/myconext/api/idp/security/webauthn/registration")
                .as(Map.class);

        Map clientData = objectMapper.readValue(readFile("webauthn/clientData.json"), Map.class);
        clientData.put("challenge", res.get("challenge"));
        String base64EncodeClientData = Base64.getUrlEncoder().withoutPadding().encodeToString(objectMapper.writeValueAsString(clientData).getBytes());

        AuthenticatorAttestationResponse response =
                AuthenticatorAttestationResponse.builder()
                        .attestationObject(ByteArray.fromBase64Url(attestation))
                        .clientDataJSON(ByteArray.fromBase64Url(base64EncodeClientData))
                        .build();
        ClientRegistrationExtensionOutputs clientExtensions = ClientRegistrationExtensionOutputs.builder().build();
        PublicKeyCredential<AuthenticatorResponse, ClientExtensionOutputs> pkc = PublicKeyCredential.builder()
                .id(ByteArray.fromBase64Url(hash()))
                .response(response)
                .clientExtensionResults(clientExtensions)
                .type(PublicKeyCredentialType.PUBLIC_KEY)
                .build();

        Map<String, Object> postData = new HashMap<>();
        postData.put("token", token);
        postData.put("credentials", objectMapper.writeValueAsString(pkc));
        given()
                .when()
                .body(postData)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/myconext/api/idp/security/webauthn/registration")
                .then()
                .body("location", equalTo("http://localhost:3001/security"));

        user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(2, user.getPublicKeyCredentials().size());
    }

    @Test
    public void knownAccount() {
        List<String> loginOptions = given()
                .when()
                .body(Map.of("email", "jdoe@example.com"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/myconext/api/idp/service/email")
                .as(new TypeRef<>() {

                });
        assertEquals(List.of(
                LoginOptions.APP.getValue(),
                LoginOptions.FIDO.getValue(),
                LoginOptions.PASSWORD.getValue(),
                LoginOptions.CODE.getValue()), loginOptions);
    }

    @Test
    public void webAuhthRegistrationNewUserHandle() throws Base64UrlException, IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        String userHandle = user.getUserHandle();
        user.setUserHandle(null);
        userRepository.save(user);

        String token = given().when().get("/myconext/api/sp/security/webauthn")
                .then().extract().body().jsonPath().getString("token");
        String attestation = "o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YVj_SZYN5YgOjGh0NBcPZHZgW4_krrmihjLHmVzzuoMdl2NFXtS5-K3OAAI1vMYKZIsLJfHwVQMAewFBb2GSzftwFaO2t01mWWDmvgjYzJfp9YL55miRnO9cO4jnIIyPjPgXY7XoJCXHf70tN9cMrM8haGb8D1TfpS-1ijer4ChFgDypfhNGUfAxbfs7mk-IQR3gS8Afxfks17mE68fid_4lZt7lOi6Z9-RiWx_m6BbX0OjViaUBAgMmIAEhWCAyic9YLBoxd3zvxePnDbBktATOgR5jBvrUFdPptlyg-SJYIFv4tjumhccPrGTFkUBvtbQKhrDZcGWdIBhFlF_JtGIR";

        given()
                .when()
                .body(Collections.singletonMap("token", token))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/myconext/api/idp/security/webauthn/registration");
        User userFromDb = userRepository.findOneUserByEmail("jdoe@example.com");
        String newUserHandle = userFromDb.getUserHandle();
        assertNotEquals(userHandle, newUserHandle);
    }

    @Test
    public void webAuhthAuthentication() throws Base64UrlException, IOException {
        String authenticationRequestId = samlAuthnRequest();
        Map<String, Object> body = new HashMap<>();
        body.put("email", "jdoe@example.com");
        body.put("authenticationRequestId", authenticationRequestId);
        given()
                .when()
                .body(body)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/myconext/api/idp/security/webauthn/authentication");

        body.put("rememberMe", false);

        Map authentication = objectMapper.readValue(readFile("webauthn/authentication.json"), Map.class);
        Map credentials = objectMapper.readValue((String) authentication.get("credentials"), Map.class);
        Map<String, String> responseMap = (Map) credentials.get("response");
        AuthenticatorAssertionResponse authenticatorAssertionResponse =
                AuthenticatorAssertionResponse.builder()
                        .authenticatorData(ByteArray.fromBase64Url(responseMap.get("authenticatorData")))
                        .clientDataJSON(ByteArray.fromBase64Url(responseMap.get("clientDataJSON")))
                        .signature(ByteArray.fromBase64Url(responseMap.get("signature")))
                        .userHandle(ByteArray.fromBase64Url(responseMap.get("userHandle")))
                        .build();

        ClientAssertionExtensionOutputs clientExtensions = ClientAssertionExtensionOutputs.builder().build();
        PublicKeyCredential<AuthenticatorResponse, ClientExtensionOutputs> pkc = PublicKeyCredential.builder()
                .id(ByteArray.fromBase64Url((String) credentials.get("id")))
                .response(authenticatorAssertionResponse)
                .clientExtensionResults(clientExtensions)
                .type(PublicKeyCredentialType.PUBLIC_KEY)
                .build();
        body.put("credentials", objectMapper.writeValueAsString(pkc));
        //We can't use the original challenge as the signature is based on challenge
        Challenge challenge = challengeRepository.findByToken(authenticationRequestId).get();
        String challengeFromServer = (String) objectMapper.readValue(Base64.getUrlDecoder().decode(responseMap.get("clientDataJSON")), Map.class).get("challenge");
        ReflectionTestUtils.setField(challenge, "challenge", challengeFromServer);
        challengeRepository.save(challenge);

        ValidatableResponse validatableResponse = given()
                .when()
                .body(body)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/myconext/api/idp/security/webauthn/authentication")
                .then();

        Response response = magicResponse(new ClientAuthenticationResponse(authenticationRequestId, validatableResponse));
        String saml = samlAuthnResponse(response, Optional.empty());

        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertTrue(saml.contains("Attribute Name=\"urn:mace:eduid.nl:1.1\""));

        //EntityId of the new eduID is based on the scoping in src/test/resources/authn_request.xml
        String eduId = user.getEduIDS().stream()
                .filter(eduID -> eduID.getServices().stream().anyMatch(s -> s.getEntityId().equals("https://manage.surfconext.nl/shibboleth")))
                .findFirst().get()
                .getValue();
        //Ensure the other eduID's have been migrated to the new multiple Services data model
        assertEquals(3, user.getEduIDS().size());
        user.getEduIDS().forEach(otherEduID -> {
            assertNull(otherEduID.getServiceProviderEntityId());
            assertEquals(1, otherEduID.getServices().size());
            assertNotNull(otherEduID.getServices().get(0).getEntityId());
        });
        assertTrue(saml.contains(eduId));
    }

    @Test
    public void testWebAuthnUrl() {
        Map map = given()
                .when()
                .accept(ContentType.JSON)
                .get("/myconext/api/sp/testWebAuthnUrl")
                .as(Map.class);
        String url = (String) map.get("url");
        assertTrue(url.startsWith("http://localhost:3000/webauthnTest/"));

        Matcher matcher = Pattern.compile("/webauthnTest/(.+?)\\?").matcher(url);
        matcher.find();
        String authenticationRequestId = matcher.group(1);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId).get();
        assertTrue(samlAuthenticationRequest.isTestInstance());
    }

    @Test
    public void webAuhthAuthenticationUserNotFound() {
        Map<String, Object> body = new HashMap<>();
        body.put("email", "nope");
        given()
                .when()
                .body(body)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/myconext/api/idp/security/webauthn/authentication")
                .then()
                .statusCode(404);
    }

    @Test
    public void webAuhtnRegistrationNotFound() {
        Map<String, Object> body = new HashMap<>();
        body.put("token", "nope");

        given()
                .when()
                .body(body)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .post("/myconext/api/idp/security/webauthn/registration")
                .then()
                .statusCode(404);
    }

    @Test
    public void webAuthnAuthenticationNotFound() {
        given()
                .when()
                .body(Collections.singletonMap("token", "nope"))
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .put("/myconext/api/idp/security/webauthn/registration")
                .then()
                .statusCode(404);
    }

    @Test
    public void testStepUpFlow() throws IOException {
        String authnContext = readFile("request_authn_context_affiliation_student.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);

        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId).get();
        samlAuthenticationRequest.setSteppedUp(StepUpStatus.IN_STEP_UP);
        authenticationRequestRepository.save(samlAuthenticationRequest);

        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        List<LinkedAccount> linkedAccounts = user.getLinkedAccounts();
        linkedAccounts.get(0).setEduPersonAffiliations(Arrays.asList("student@example.com"));
        user.setLinkedAccounts(linkedAccounts);
        userRepository.save(user);

        oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.PUT);

        samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId).get();
        response = given().redirects().follow(false)
                .when()
                .queryParam("h", samlAuthenticationRequest.getHash())
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .get("/saml/guest-idp/magic");
        String location = response.getHeader("Location");
        assertTrue(location.startsWith("http://localhost:3000/confirm-stepup?h="));

        when()
                .get("/myconext/api/idp/service/hash/" + samlAuthenticationRequest.getHash())
                .then()
                .body("name", equalTo("Bart TEst spd 6.00"));
        Map<String, Object> userMap = when()
                .get("/myconext/api/idp/me/" + samlAuthenticationRequest.getHash())
                .as(new TypeRef<>() {
                });
        assertEquals("jdoe@example.com", userMap.get("email"));
        when()
                .get("/myconext/api/idp/service/id/" + samlAuthenticationRequest.getId())
                .then()
                .body("name", equalTo("Bart TEst spd 6.00"));

    }

    @Test
    public void testStepUpFlowProceedAfterFailure() throws IOException {
        String authnContext = readFile("request_authn_context_affiliation_student.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);

        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId).get();
        samlAuthenticationRequest.setSteppedUp(StepUpStatus.IN_STEP_UP);
        authenticationRequestRepository.save(samlAuthenticationRequest);

        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.PUT);

        samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId).get();
        response = given().redirects().follow(false)
                .when()
                .queryParam("h", samlAuthenticationRequest.getHash())
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .get("/saml/guest-idp/magic");
        String body = response.getBody().asString();
        assertTrue(body.contains("Since your browser does not support JavaScript"));
    }

    @Test
    public void testResetPasswordLinkWithExistingEmailChangeRequest() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        changeEmailHashRepository.save(new ChangeEmailHash(user, "new@example.com", "hash"));
        given()
                .when()
                .accept(ContentType.JSON)
                .put("/myconext/api/sp/reset-password-link")
                .as(Map.class);
        assertEquals(0, changeEmailHashRepository.findByUserId(user.getId()).size());
    }

    @Test
    public void testResetPasswordLink() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.deletePassword();
        userRepository.save(user);

        given()
                .when()
                .accept(ContentType.JSON)
                .put("/myconext/api/sp/reset-password-link")
                .as(Map.class);
        assertEquals(1, passwordResetHashRepository.findByUserId(user.getId()).size());
    }

    @Test
    public void updateEmail() {
        given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("email", "changed@example.com"))
                .put("/myconext/api/sp/email")
                .then()
                .statusCode(HttpStatus.CREATED.value());
        ChangeEmailHash changeEmailHash = changeEmailHashRepository.findAll().get(0);

        Map map = given()
                .when()
                .accept(ContentType.JSON)
                .queryParam("h", changeEmailHash.getHash())
                .get("/myconext/api/sp/confirm-email")
                .as(Map.class);
        assertEquals("changed@example.com", map.get("email"));
    }

    @Test
    public void institutionNames() throws IOException {
        IdentityProvider identityProvider = given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .queryParam("schac_home", "sub6.aap.nl")
                .get("/myconext/api/sp/institution/names")
                .as(IdentityProvider.class);

        assertEquals("thkidp EN", identityProvider.getName());
        assertEquals("thkidp NL", identityProvider.getNameNl());
    }

    @Test
    public void outstandingEmailLinks() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        changeEmailHashRepository.save(new ChangeEmailHash(user, "jdoe@new.com", "hash"));

        given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/outstanding-email-links")
                .then()
                .body(equalTo("true"));
    }

    @Test
    public void updateEmailWithOutstandingPasswordResetRequest() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        passwordResetHashRepository.save(new PasswordResetHash(user, "hash"));

        given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("email", "changed@example.com"))
                .put("/myconext/api/sp/email")
                .then()
                .statusCode(HttpStatus.NOT_ACCEPTABLE.value());
    }

    @Test
    public void updateEmailWithOutstandingPasswordResetRequestWithForce() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        passwordResetHashRepository.save(new PasswordResetHash(user, "hash"));

        given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("email", "changed@example.com"))
                .queryParam("force", "true")
                .put("/myconext/api/sp/email")
                .then()
                .statusCode(HttpStatus.CREATED.value());
        assertEquals(0, passwordResetHashRepository.findByUserId(user.getId()).size());
    }

    @Test
    public void updateEmailWithExistingEmail() {
        given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("email", "mdoe@example.com"))
                .queryParam("force", "true")
                .put("/myconext/api/sp/email")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void updateLinkedAccount() {
        UpdateLinkedAccountRequest updateLinkedAccountRequest = new UpdateLinkedAccountRequest("guest@example.nl", null, false, null, "schac");
        given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(updateLinkedAccountRequest)
                .put("/myconext/api/sp/prefer-linked-account")
                .as(Map.class);
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        Optional<LinkedAccount> optionalLinkedAccount = user.getLinkedAccounts().stream().filter(LinkedAccount::isPreferred).findFirst();
        assertTrue(optionalLinkedAccount.isPresent());
    }

    @Test
    public void updateExternalLinkedAccount() {
        Date today = new Date();
        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount(
                "subjectId",
                IdpScoping.eherkenning,
                new VerifyIssuer("id", "name", "logo"),
                Verification.Geverifieerd,
                "serviceUUID",
                "serviceID",
                "subjectIssuer",
                List.of("brinCode"),
                "initials",
                "chosenName",
                "firstName",
                "preferredLastName",
                "legalLastName",
                "partnerLastNamePrefix",
                "legalLastNamePrefix",
                "preferredLastNamePrefix",
                "partnerLastName",
                today,
                today,
                Date.from(today.toInstant().plus(30, ChronoUnit.DAYS)),
                true
        );
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        userRepository.save(user);

        UpdateLinkedAccountRequest updateLinkedAccountRequest = new UpdateLinkedAccountRequest(null, null, true, IdpScoping.eherkenning.name(), null);
        given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(updateLinkedAccountRequest)
                .put("/myconext/api/sp/prefer-linked-account")
                .as(Map.class);

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");
        assertTrue(userFromDB.getLinkedAccounts().stream().noneMatch(LinkedAccount::isPreferred));
        assertTrue(userFromDB.getExternalLinkedAccounts().stream().allMatch(ExternalLinkedAccount::isPreferred));
        assertEquals("firstName", userFromDB.getDerivedGivenName());
        assertEquals("legalLastNamePrefix legalLastName", userFromDB.getDerivedFamilyName());
    }

    @Test
    public void metaData() {
        String xml = given().redirects().follow(false)
                .when()
                .get("/saml/guest-idp/metadata")
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .response()
                .asString();
        assertTrue(xml.contains("eduID IdP"));
    }

    @Test
    public void createUserControlCode() {
        clearExternalAccounts("jdoe@example.com");
        ControlCode controlCode = new ControlCode("Lee", "Harpers", "01 Mar 1977");
        ControlCode responseControlCode =given()
                .body(controlCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/myconext/api/sp/control-code")
                .as(ControlCode.class);
        assertEquals(5, responseControlCode.getCode().length());
        User user = userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com").get();
        assertEquals(user.getControlCode().getCode(), responseControlCode.getCode());

        Map<String, Object> userResponse = given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .when()
                .delete("/myconext/api/sp/control-code")
                .as(new TypeRef<>() {
                });
        assertFalse(userResponse.containsKey("controlCode"));

        User userFromDB = userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com").get();
        assertNull(userFromDB.getControlCode());
    }

    @Test
    public void createUserControlCodeForbidden() {
        ControlCode controlCode = new ControlCode("Lee", "Harpers", "01 Mar 1977");
        given()
                .body(controlCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/myconext/api/sp/control-code")
                .then()
                .statusCode(403);
    }

    public void errorMailOverload() {
        Map<String, Object> json = Map.of("error", "unexpected");
        CookieFilter cookieFilter = new CookieFilter();
        for (int i = 0; i < 10; i++) {
            given()
                    .when()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(json)
                    .filter(cookieFilter)
                    .post("/myconext/api/sp/error")
                    .then()
                    .statusCode(HttpStatus.CREATED.value());
        }
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .filter(cookieFilter)
                .post("/myconext/api/sp/error")
                .then()
                .statusCode(HttpStatus.UNPROCESSABLE_ENTITY.value());
    }


    private String hash() {
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
