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
import myconext.tiqr.SURFSecureID;
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
import static myconext.security.GuestIdpAuthenticationRequestFilter.GUEST_IDP_REMEMBER_ME_COOKIE_NAME;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

@SuppressWarnings("unchecked")
public class UserControllerTest extends AbstractIntegrationTest {

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private ChallengeRepository challengeRepository;

    @Test
    public void existingUser() throws IOException {
        MagicLinkResponse magicLinkResponse = magicLinkRequest(user("jdoe@example.com"), HttpMethod.PUT);

        when()
                .get("/myconext/api/idp/service/name/" + magicLinkResponse.authenticationRequestId)
                .then()
                .body("name", equalTo("https://manage.surfconext.nl/shibboleth"));

        magicLinkResponse.response
                .statusCode(HttpStatus.CREATED.value());
        String samlResponse = samlResponse(magicLinkResponse);
        assertTrue(samlResponse.contains("jdoe@example.com"));
        assertTrue(samlResponse.contains("urn:oasis:names:tc:SAML:attribute:subject-id"));

        when()
                .get("/myconext/api/idp/resend_magic_link_request?id=" + magicLinkResponse.authenticationRequestId)
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    public void newUserNotFound() throws IOException {
        magicLinkRequest(user("new@example.com"), HttpMethod.PUT)
                .response
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void duplicateEmailNewUser() throws IOException {
        magicLinkRequest(user("jdoe@example.com"), HttpMethod.POST)
                .response
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void newUserProvisioned() throws IOException {
        User user = user("new@example.com", "Mary", "Doe", "en");

        MagicLinkResponse magicLinkResponse = magicLinkRequest(user, HttpMethod.POST);
        assertEquals(user.getGivenName(), userRepository.findUserByEmail(user.getEmail()).get().getGivenName());

        String samlResponse = samlResponse(magicLinkResponse);
        assertTrue(samlResponse.contains("new@example.com"));

        when()
                .get("/myconext/api/idp/resend_magic_link_request?id=" + magicLinkResponse.authenticationRequestId)
                .then()
                .statusCode(HttpStatus.OK.value());

    }

    @Test
    public void authenticationRequestExpired() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        doExpireWithFindProperty(SamlAuthenticationRequest.class, "id", authenticationRequestId);

        MagicLinkRequest linkRequest = new MagicLinkRequest(authenticationRequestId,
                user("new@example.com", "Mary", "Doe", "en"), false);

        magicLinkRequest(linkRequest, HttpMethod.POST)
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

        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        String samlResponse = samlResponse(magicLinkResponse);

        assertTrue(samlResponse.contains(ACR.LINKED_INSTITUTION));
    }

    @Test
    public void accountLinkingWithValidatedNames() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");

        String authnContext = readFile("request_authn_context_validated_name.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);

        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        String samlResponse = samlResponse(magicLinkResponse);

        assertTrue(samlResponse.contains(ACR.VALIDATE_NAMES));
        assertTrue(samlResponse.contains(user.getFamilyName()));
        assertTrue(samlResponse.contains(user.getGivenName()));
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

        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        String samlResponse = this.samlResponse(magicLinkResponse);

        assertTrue(samlResponse.contains("Your institution has not provided this affiliation"));
        assertTrue(samlResponse.contains("urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext"));
        assertTrue(samlResponse.contains("<saml2:AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:unspecified</saml2:AuthnContextClassRef>"));
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

        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        String samlResponse = this.samlResponse(magicLinkResponse);

        assertTrue(samlResponse.contains("Your institution has not provided those attributes"));
        assertTrue(samlResponse.contains("urn:oasis:names:tc:SAML:2.0:status:NoAuthnContext"));
    }

    @Test
    public void invalidLoaLevel() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        String authnContext = readFile("request_authn_context_invalid.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);

        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);
        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false), HttpMethod.PUT);
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
                .body("size()", is(5));
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
        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false), HttpMethod.POST);
        Response response = magicResponse(magicLinkResponse);
        assertTrue(IOUtils.toString(response.asInputStream(), Charset.defaultCharset()).contains("Nice"));
    }

    @Test
    public void updateUser() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.setGivenName("Mary");
        user.setFamilyName("Poppins");
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(user)
                .put("/myconext/api/sp/update")
                .then()
                .statusCode(HttpStatus.CREATED.value());

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");

        assertEquals(user.getGivenName(), userFromDB.getGivenName());
        assertEquals(user.getFamilyName(), userFromDB.getFamilyName());
    }

    @Test
    public void removeUserLinkedAccounts() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(2, user.getLinkedAccounts().size());

        LinkedAccount linkedAccount = user.getLinkedAccounts().get(0);
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(linkedAccount)
                .put("/myconext/api/sp/institution")
                .then()
                .statusCode(HttpStatus.OK.value());

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");

        assertEquals(1, userFromDB.getLinkedAccounts().size());
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
        EduID eduID = user.getEduIDS().stream().filter(val -> val.getServiceProviderEntityId().equals(("http://mock-sp"))).findFirst().get();
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new DeleteService(eduID.getServiceProviderEntityId(), new ArrayList<>()))
                .put("/myconext/api" +
                        "/sp/service")
                .then()
                .statusCode(HttpStatus.OK.value());

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");

        assertFalse(userFromDB.getEduIDS().stream().anyMatch(val -> val.getServiceProviderEntityId().equals(("http://mock-sp"))));
    }

    @Test
    public void updateUser403() {
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new User())
                .put("/myconext/api/sp/update")
                .then()
                .statusCode(400);
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
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(-1, random);

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

        Optional<User> optionalUser = userRepository.findUserByEmail("jdoe@example.com");
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
        MagicLinkRequest magicLinkRequest = new MagicLinkRequest(authenticationRequestId, user, true);

        CookieFilter cookieFilter = new CookieFilter();
        Response response = given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(magicLinkRequest)
                .filter(cookieFilter)
                .put("/myconext/api/idp/magic_link_request");

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
    }

    @Test
    public void loginWithWrongPassword() throws IOException {
        User user = user("mdoe@example.com");
        userSetPassword(user, "nope");
        String authenticationRequestId = samlAuthnRequest();
        MagicLinkRequest magicLinkRequest = new MagicLinkRequest(authenticationRequestId, user, true);

        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(magicLinkRequest)
                .put("/myconext/api/idp/magic_link_request")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void magicLinkCanNotBeReused() throws IOException {
        MagicLinkResponse magicLinkResponse = magicLinkRequest(user("jdoe@example.com"), HttpMethod.PUT);
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
    public void magicLinkRequiresSameBrowser() throws IOException {
        SamlAuthenticationRequest samlAuthenticationRequest = doMagicLinkRequiredSameBrowser();
        Response response = given().redirects().follow(false)
                .when()
                .queryParam("id", samlAuthenticationRequest.getId())
                .queryParam("verificationCode", samlAuthenticationRequest.getVerificationCode())
                .get("/saml/guest-idp/continue");
        String saml = samlAuthnResponse(response, Optional.empty());

        assertTrue(saml.contains("jdoe@example.com"));
    }

    @Test
    public void magicLinkRequiresSameBrowseWrongCode() throws IOException {
        SamlAuthenticationRequest samlAuthenticationRequest = doMagicLinkRequiredSameBrowser();
        given().redirects().follow(false)
                .when()
                .queryParam("id", samlAuthenticationRequest.getId())
                .queryParam("verificationCode", "NOP-123")
                .queryParam("currentUrl", "https://expected")
                .get("/saml/guest-idp/continue")
                .then()
                .statusCode(302)
                .header("Location", equalTo("https://expected&mismatch=true"));
    }

    @Test
    public void magicLinkRequiresSameBrowseWrongCodeMaxRetry() throws IOException {
        SamlAuthenticationRequest samlAuthenticationRequest = doMagicLinkRequiredSameBrowser();
        samlAuthenticationRequest.setRetryVerificationCode(3);
        authenticationRequestRepository.save(samlAuthenticationRequest);

        given().redirects().follow(false)
                .when()
                .queryParam("id", samlAuthenticationRequest.getId())
                .queryParam("verificationCode", "NOP-123")
                .queryParam("currentUrl", "https://expected")
                .get("/saml/guest-idp/continue")
                .then()
                .statusCode(302)
                .header("Location", equalTo("http://localhost:3000/max-attempts"));
    }

    @Test
    public void magicLinkRequiresSameBrowseNotLoggedIn() throws IOException {
        SamlAuthenticationRequest samlAuthenticationRequest = doMagicLinkRequiredSameBrowser();
        samlAuthenticationRequest.setLoginStatus(LoginStatus.NOT_LOGGED_IN);
        authenticationRequestRepository.save(samlAuthenticationRequest);
        given().redirects().follow(false)
                .when()
                .queryParam("id", samlAuthenticationRequest.getId())
                .queryParam("verificationCode", samlAuthenticationRequest.getVerificationCode())
                .get("/saml/guest-idp/continue")
                .then()
                .statusCode(302)
                .header("Location", equalTo("http://localhost:3000/expired"));
    }

    @Test
    public void magicLinkRequiresSameBrowseExpiredRequest() throws IOException {
        SamlAuthenticationRequest samlAuthenticationRequest = doMagicLinkRequiredSameBrowser();
        authenticationRequestRepository.delete(samlAuthenticationRequest);
        given().redirects().follow(false)
                .when()
                .queryParam("id", samlAuthenticationRequest.getId())
                .get("/saml/guest-idp/continue")
                .then()
                .statusCode(302)
                .header("Location", equalTo("http://localhost:3000/expired"));
    }

    private SamlAuthenticationRequest doMagicLinkRequiredSameBrowser() throws IOException {
        MagicLinkResponse magicLinkResponse = magicLinkRequest(user("jdoe@example.com"), HttpMethod.PUT);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();
        given().redirects().follow(false)
                .when()
                .queryParam("h", samlAuthenticationRequest.getHash())
                .get("/saml/guest-idp/magic")
                .then()
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", "http://localhost:3000/success");
        given()
                .when()
                .queryParam("id", samlAuthenticationRequest.getId())
                .get("/myconext/api/idp/security/success")
                .then()
                .body(equalTo("" + LoginStatus.LOGGED_IN_DIFFERENT_DEVICE.ordinal()));

        samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();
        return samlAuthenticationRequest;
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
        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, StringUtils.hasText(user.getPassword())), HttpMethod.PUT);

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
        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, StringUtils.hasText(user.getPassword())), HttpMethod.PUT);

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
        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false), HttpMethod.PUT);
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

        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false), HttpMethod.PUT);
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
                LoginOptions.MAGIC.getValue()), loginOptions);
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

        Response response = magicResponse(new MagicLinkResponse(authenticationRequestId, validatableResponse));
        String saml = samlAuthnResponse(response, Optional.empty());

        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertTrue(saml.contains("Attribute Name=\"urn:mace:eduid.nl:1.1\""));

        String eduId = user.getEduIDS().stream()
                .filter(eduID -> eduID.getServiceProviderEntityId().equals("https://manage.surfconext.nl/shibboleth"))
                .findFirst().get().getValue();
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

        magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false), HttpMethod.PUT);

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
                .body("name", equalTo("https://manage.surfconext.nl/shibboleth"));
        when()
                .get("/myconext/api/idp/service/id/" + samlAuthenticationRequest.getId())
                .then()
                .body("name", equalTo("https://manage.surfconext.nl/shibboleth"));

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
        magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false), HttpMethod.PUT);

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
        Map names = given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .queryParam("schac_home", "example.com")
                .get("/myconext/api/sp/institution/names")
                .as(Map.class);

        assertEquals("Mujina EN", names.get("displayNameEn"));
        assertEquals("Mujina NL", names.get("displayNameNl"));
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

    private String hash() {
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
