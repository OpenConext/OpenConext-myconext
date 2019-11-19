package surfid.api;

import io.restassured.http.Cookie;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import surfid.AbstractIntegrationTest;
import surfid.model.MagicLinkRequest;
import surfid.model.SamlAuthenticationRequest;
import surfid.model.UpdateUserRequest;
import surfid.model.User;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
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
                .statusCode(HttpStatus.GONE.value());
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
        user.merge(new User(user.getEmail(), "Mary", "Poppins", "secretA12"), passwordEncoder);
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new UpdateUserRequest(user, true, false, null))
                .put("/surfid/api/sp/update")
                .then()
                .statusCode(201);

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");

        assertEquals(user.getGivenName(), userFromDB.getGivenName());
        assertEquals(user.getFamilyName(), userFromDB.getFamilyName());
        assertTrue(passwordEncoder.matches(user.getPassword(), userFromDB.getPassword()));
    }

    @Test
    public void updateUser403() {
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new UpdateUserRequest(new User(), false, false, null))
                .put("/surfid/api/sp/update")
                .then()
                .statusCode(403);
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
