package myconext.api;

import io.restassured.http.Cookie;
import io.restassured.response.Response;
import myconext.AbstractIntegrationTest;
import myconext.model.ClientAuthenticationRequest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static myconext.security.GuestIdpAuthenticationRequestFilter.GUEST_IDP_REMEMBER_ME_COOKIE_NAME;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@ActiveProfiles(value = "dev", inheritProfiles = false)
public class LoginControllerTest extends AbstractIntegrationTest {

    @Test
    public void config() {
        given()
                .when()
                .get("/config")
                .then()
                .body("baseDomain", equalTo("test2.surfconext.nl"))
                .body("loginUrl", equalTo("http://localhost:8081/login"));
    }

    @Test
    public void register() {
        given().redirects().follow(false)
                .when()
                .get("/register")
                .then()
                .statusCode(302)
                .header("Location", "https://my.test2.surfconext.nl/Shibboleth.sso/Login?entityID=https://localhost.surf.id&lang=en");
    }

    @Test
    public void doLogin() {
        given().redirects().follow(false)
                .when()
                .get("/doLogin")
                .then()
                .statusCode(302)
                .header("Location", "https://my.test2.surfconext.nl/Shibboleth.sso/Login?entityID=https://localhost.surf.id&lang=en");
    }

    @Test
    public void doLogout() {
        String param = "logout=true";
        given().redirects().follow(false)
                .when()
                .queryParam("param", param)
                .get("/doLogout")
                .then()
                .statusCode(302)
                .header("Location", "http://localhost:3001/landing?logout=true");
    }

    @Test
    public void doLogoutAfterDelete() {
        String param = "delete=true";
        given().redirects().follow(false)
                .when()
                .queryParam("param", param)
                .cookie("TEST", "value")
                .get("/doLogout")
                .then()
                .statusCode(302)
                .header("Location", "http://localhost:3001/landing?delete=true")
                .cookie("TEST", equalTo(""));
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