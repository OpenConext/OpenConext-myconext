package surfid.api;

import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.CookieStore;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import surfid.AbstractIntegrationTest;
import surfid.model.MagicLinkRequest;
import surfid.model.SamlAuthenticationRequest;
import surfid.model.UpdateUserSecurityRequest;
import surfid.model.User;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static surfid.security.GuestIdpAuthenticationRequestFilter.BROWSER_SESSION_COOKIE_NAME;
import static surfid.security.GuestIdpAuthenticationRequestFilter.GUEST_IDP_REMEMBER_ME_COOKIE_NAME;

public class UserControllerTest extends AbstractIntegrationTest {

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    public void existingUser() throws IOException {
        MagicLinkResponse magicLinkResponse = magicLinkRequest(new User("jdoe@example.com"), HttpMethod.PUT);
        magicLinkResponse.response
                .statusCode(HttpStatus.CREATED.value());
        String samlResponse = samlResponse(magicLinkResponse);
        assertTrue(samlResponse.contains("jdoe@example.com"));
    }

    @Test
    public void newUserNotFound() throws IOException {
        magicLinkRequest(new User("new@example.com"), HttpMethod.PUT)
                .response
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void duplicateEmailNewUser() throws IOException {
        magicLinkRequest(new User("jdoe@example.com"), HttpMethod.POST)
                .response
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    public void newUserProvisioned() throws IOException {
        User user = new User("new@example.com", "Mary", "Doe");

        MagicLinkResponse magicLinkResponse = magicLinkRequest(user, HttpMethod.POST);
        assertEquals(user.getGivenName(), userRepository.findUserByEmail(user.getEmail()).get().getGivenName());

        String samlResponse = samlResponse(magicLinkResponse);
        assertTrue(samlResponse.contains("new@example.com"));
    }

    @Test
    public void newUserProvisionedWithPassword() throws IOException {
        User user = new User("new@example.com", "Mary", "Doe", "secretA12");

        magicLinkRequest(user, HttpMethod.POST);
        user = userRepository.findUserByEmail(user.getEmail()).get();
        assertTrue(this.passwordEncoder.matches("secretA12", user.getPassword()));
    }

    @Test
    public void authenticationRequestExpired() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        doExpireWithFindProperty(SamlAuthenticationRequest.class, "id", authenticationRequestId);

        MagicLinkRequest linkRequest = new MagicLinkRequest(authenticationRequestId,
                new User("new@example.com", "Mary", "Doe"), false, false);

        magicLinkRequest(linkRequest, HttpMethod.POST)
                .response
                .statusCode(HttpStatus.FOUND.value())
                .header("Location", "http://localhost:3000/expired");
    }

    @Test
    public void rememberMe() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        User user = new User("steve@example.com", "Steve", "Doe");
        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, true, false), HttpMethod.POST);
        Response response = magicResponse(magicLinkResponse);

        String cookie = response.cookie(GUEST_IDP_REMEMBER_ME_COOKIE_NAME);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(cookie).orElseThrow(IllegalArgumentException::new);

        assertEquals(true, samlAuthenticationRequest.isRememberMe());

        String saml = samlAuthnResponse(samlAuthnRequestResponse(new Cookie.Builder(GUEST_IDP_REMEMBER_ME_COOKIE_NAME, cookie).build(), null));
        assertTrue(saml.contains("steve@example.com<"));
    }

    @Test
    public void relayState() throws IOException {
        User user = new User("steve@example.com", "Steve", "Doe");
        String authenticationRequestId = samlAuthnRequest("Nice");
        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false, false), HttpMethod.POST);
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
                .put("/surfid/api/sp/update")
                .then()
                .statusCode(201);

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");

        assertEquals(user.getGivenName(), userFromDB.getGivenName());
        assertEquals(user.getFamilyName(), userFromDB.getFamilyName());
    }

    @Test
    public void updateUser403() {
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new User())
                .put("/surfid/api/sp/update")
                .then()
                .statusCode(403);
    }

    @Test
    public void updateUserWeakPassword() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new UpdateUserSecurityRequest(user.getId(), true, false, null, "secret"))
                .put("/surfid/api/sp/security")
                .then()
                .statusCode(422);
    }

    @Test
    public void updateUserWrongPassword() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        ReflectionTestUtils.setField(user, "password", "abcdefghijklmnop");
        userRepository.save(user);

        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new UpdateUserSecurityRequest(user.getId(), true, false, "nope", "nope"))
                .put("/surfid/api/sp/security")
                .then()
                .statusCode(403);
    }

    @Test
    public void clearPassword() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        String password = "abcdefghijklmnop";
        ReflectionTestUtils.setField(user, "password", passwordEncoder.encode(password));
        userRepository.save(user);

        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new UpdateUserSecurityRequest(user.getId(), false, true, password, null))
                .put("/surfid/api/sp/security")
                .then()
                .statusCode(201);

        user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertNull(user.getPassword());
    }

    @Test
    public void deleteUser() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(user)
                .delete("/surfid/api/sp/delete")
                .then()
                .statusCode(201);

        Optional<User> optionalUser = userRepository.findUserByEmail("jdoe@example.com");
        assertFalse(optionalUser.isPresent());
    }

    @Test
    public void loginWithPassword() throws IOException {
        User user = new User("mdoe@example.com", null, null, "Secret123");
        String authenticationRequestId = samlAuthnRequest();
        MagicLinkRequest magicLinkRequest = new MagicLinkRequest(authenticationRequestId, user, false, true);

        Response response = given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(magicLinkRequest)
                .put("/surfid/api/idp/magic_link_request");

        String url = (String) response.body().as(Map.class).get("url");
        url = url.replace("8081", this.port + "");

        CookieFilter cookieFilter = new CookieFilter();
        response = given()
                .redirects().follow(false)
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .filter(cookieFilter)
                .get(url);
        String html = samlAuthnResponse(response);
        assertTrue(html.contains("mdoe@example.com"));

        //now that we are logged in we can use the JSESSION COOKIE to test SSO
        CookieStore cookieStore = (CookieStore) ReflectionTestUtils.getField(cookieFilter, "cookieStore");
        org.apache.http.cookie.Cookie cookie = cookieStore.getCookies().get(0);
        response = samlAuthnRequestResponse(new Cookie.Builder(cookie.getName(), cookie.getValue()).build(), "relay");
        html = samlAuthnResponse(response);
        assertTrue(html.contains("mdoe@example.com"));
    }

    @Test
    public void loginWithWrongPassword() throws IOException {
        User user = new User("mdoe@example.com", null, null, "nope");
        String authenticationRequestId = samlAuthnRequest();
        MagicLinkRequest magicLinkRequest = new MagicLinkRequest(authenticationRequestId, user, false, true);

        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(magicLinkRequest)
                .put("/surfid/api/idp/magic_link_request")
                .then()
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    public void magicLinkCanNotBeReused() throws IOException {
        MagicLinkResponse magicLinkResponse = magicLinkRequest(new User("jdoe@example.com"), HttpMethod.PUT);
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

    private String samlResponse(MagicLinkResponse magicLinkResponse) throws IOException {
        Response response = magicResponse(magicLinkResponse);

        return samlAuthnResponse(response);
    }

    private String samlAuthnResponse(Response response) throws IOException {
        assertEquals(200, response.getStatusCode());
        String html = IOUtils.toString(response.asInputStream(), Charset.defaultCharset());

        Matcher matcher = Pattern.compile("name=\"SAMLResponse\" value=\"(.*?)\"").matcher(html);
        matcher.find();
        return new String(Base64.getDecoder().decode(matcher.group(1)));
    }

    private Response magicResponse(MagicLinkResponse magicLinkResponse) throws UnsupportedEncodingException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();
        Response response = given().redirects().follow(false)
                .when()
                .queryParam("h", samlAuthenticationRequest.getHash())
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .get("/saml/guest-idp/magic");

        if (response.getStatusCode() == 302) {
            //new user confirmation screen
            String uri = response.getHeader("Location");
            MultiValueMap<String, String> parameters = UriComponentsBuilder.fromUriString(uri).build().getQueryParams();
            String redirect = URLDecoder.decode(parameters.getFirst("redirect"), Charset.defaultCharset().name());
            redirect = redirect.replace("8081", this.port + "");
            String h = parameters.getFirst("h");
            response = given().when()
                    .queryParam("h", h)
                    .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                    .get(redirect);
        }
        return response;
    }


    private MagicLinkResponse magicLinkRequest(User user, HttpMethod method) throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        return magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false, StringUtils.hasText(user.getPassword())), method);
    }

    private MagicLinkResponse magicLinkRequest(MagicLinkRequest linkRequest, HttpMethod method) {
        RequestSpecification requestSpecification = given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(linkRequest);

        String path = "/surfid/api/idp/magic_link_request";
        Response response = method.equals(HttpMethod.POST) ? requestSpecification.post(path) : requestSpecification.put(path);
        return new MagicLinkResponse(linkRequest.getAuthenticationRequestId(), response.then());
    }

}
