package myconext.api;

import io.restassured.http.Cookie;
import io.restassured.response.Response;
import myconext.AbstractIntegrationTest;
import myconext.model.ClientAuthenticationRequest;
import myconext.model.User;
import org.junit.Test;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static myconext.security.GuestIdpAuthenticationRequestFilter.GUEST_IDP_REMEMBER_ME_COOKIE_NAME;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@ActiveProfiles(value = "dev", inheritProfiles = false)
public class LoginControllerTest extends AbstractIntegrationTest {

    @Autowired
    private LoginController loginController;

    @Before
    public void resetCreateFromInstitutionReturnUrlAllowList() {
        ReflectionTestUtils.setField(loginController, "createFromInstitutionAllowedReturnDomains", List.of());
    }

    @Test
    public void config() {
        given()
                .when()
                .get("/config")
                .then()
                .body("baseDomain", equalTo("test2.surfconext.nl"))
                .body("loginUrl", equalTo("http://localhost:8081/myconext/api/sp/login"));
    }

    @Test
    public void register() {
        given().redirects().follow(false)
                .when()
                .get("/register")
                .then()
                .statusCode(302)
                // Todo: verify
                .header("Location", "https://my.test2.surfconext.nl/oauth2/authorization/oidcng?lang=en");
    }

    @Test
    public void registerWithEnrollmentVerificationKey() {
        User user = userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com").get();
        String enrollmentVerificationKey = UUID.randomUUID().toString();
        user.setEnrollmentVerificationKey(enrollmentVerificationKey);
        userRepository.save(user);

        given()
                .redirects().follow(false)
                .when()
                .pathParam("enrollmentVerificationKey", enrollmentVerificationKey)
                .get("/register/{enrollmentVerificationKey}")
                .then()
                .statusCode(302)
                .cookie("login_preference", "useApp")
                .cookie("username", user.getEmail())
                .header("Location",
                        "http://localhost:3001/security");

        user = userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com").get();
        assertNull(user.getEnrollmentVerificationKey());
    }

    @Test
    public void testCreateFromInstitutionLogin() {
        User user = userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com").get();
        user.setNewUser(false);
        String createFromInstitutionKey = UUID.randomUUID().toString();
        user.setCreateFromInstitutionKey(createFromInstitutionKey);
        userRepository.save(user);
        given()
                .redirects().follow(false)
                .when()
                .queryParam("key", createFromInstitutionKey)
                .get("/create-from-institution-login")
                .then()
                .statusCode(302)
                .cookie("username", user.getEmail())
                .header("Location",
                        "http://localhost:3001/security?new=false");

        user = userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com").get();
        assertNull(user.getCreateFromInstitutionKey());
    }

    @Test
    public void testCreateFromInstitutionLoginNewUser() {
        User user = userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com").get();
        String createFromInstitutionKey = UUID.randomUUID().toString();
        user.setCreateFromInstitutionKey(createFromInstitutionKey);
        user.setNewUser(true);
        userRepository.save(user);
        given()
                .redirects().follow(false)
                .when()
                .queryParam("key", createFromInstitutionKey)
                .get("/create-from-institution-login")
                .then()
                .statusCode(302)
                .cookie("username", user.getEmail())
                .header("Location",
                        "http://localhost:3001/security?new=true");
    }

    @Test
    public void testCreateFromInstitutionLoginRedirectsToAllowedReturnUrl() {
        ReflectionTestUtils.setField(loginController, "createFromInstitutionAllowedReturnDomains", List.of("myuniversity.nl"));

        User user = userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com").get();
        String createFromInstitutionKey = UUID.randomUUID().toString();
        user.setCreateFromInstitutionKey(createFromInstitutionKey);
        user.setCreateFromInstitutionReturnUrl("https://sitte.myuniversity.nl/landing");
        user.setNewUser(false);
        userRepository.save(user);

        given()
                .redirects().follow(false)
                .when()
                .queryParam("key", createFromInstitutionKey)
                .get("/create-from-institution-login")
                .then()
                .statusCode(302)
                .header("Location", "https://sitte.myuniversity.nl/landing?new=false");

        user = userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com").get();
        assertNull(user.getCreateFromInstitutionKey());
        assertNull(user.getCreateFromInstitutionReturnUrl());
    }

    @Test
    public void redirectToSPServiceDeskHook() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        User user = user("steve@example.com", "Steve", "Doe", "en");
        ClientAuthenticationRequest clientAuthenticationRequest = new ClientAuthenticationRequest(authenticationRequestId, user, false, "response");

        ClientAuthenticationResponse magicLinkResponse = oneTimeLoginCodeRequest(clientAuthenticationRequest, HttpMethod.POST);
        Response response = magicResponse(magicLinkResponse);

        String cookie = response.cookie(GUEST_IDP_REMEMBER_ME_COOKIE_NAME);
        String authnContext = readFile("request_authn_context.xml");
        samlAuthnRequestResponseWithLoa(
                new Cookie.Builder(GUEST_IDP_REMEMBER_ME_COOKIE_NAME, cookie).build(), null, authnContext);
        String location = given().redirects().follow(false)
                .when()
                .get("/servicedesk/" + authenticationRequestId)
                .header("Location");
        assertEquals("http://localhost:3001/personal?servicedesk=start", location);
    }

    @Test
    public void redirectToSPServiceDeskHookNoAuthenticationRequest() throws IOException {
        String location = given()
                .redirects()
                .follow(false)
                .when()
                .get("/servicedesk/" + UUID.randomUUID().toString())
                .header("Location");
        assertEquals("http://localhost:3000/expired", location);
    }

}
