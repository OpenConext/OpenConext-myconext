package surfid.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import surfid.AbstractIntegrationTest;
import surfid.model.MagicLinkRequest;
import surfid.model.SamlAuthenticationRequest;
import surfid.model.User;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserControllerTest extends AbstractIntegrationTest {

    @Test
    public void existingUser() throws IOException {
        MagicLinkResponse magicLinkResponse = magicLinkRequest(new User("jdoe@example.com"), HttpMethod.PUT);
        magicLinkResponse.response
                .body("givenName", equalTo("John"))
                .body("familyName", equalTo("Doe"));

        String samlResponse = samlResponse(magicLinkResponse);
        assertTrue(samlResponse.contains("jdoe@example.com"));
    }

    private String samlResponse(MagicLinkResponse magicLinkResponse) throws IOException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();
        Response response = given().redirects().follow(false)
                .when()
                .queryParam("h", samlAuthenticationRequest.getHash())
                .get("/saml/guest-idp/magic");

        assertEquals(200, response.getStatusCode());
        String html = IOUtils.toString(response.asInputStream(), Charset.defaultCharset());

        Matcher matcher = Pattern.compile("name=\"SAMLResponse\" value=\"(.*?)\"").matcher(html);
        matcher.find();
        return new String(Base64.getDecoder().decode(matcher.group(1)));
    }

    @Test
    public void newUserNotFound() throws IOException {
        magicLinkRequest(new User("new@example.com"), HttpMethod.PUT)
                .response
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void newUserProvisioned() throws IOException {
        User user = new User("new@example.com", "Mary", "Doe");
        MagicLinkResponse magicLinkResponse = magicLinkRequest(user, HttpMethod.POST);
        magicLinkResponse.response
                .body("givenName", equalTo("Mary"))
                .body("familyName", equalTo("Doe"));
        assertEquals(user.getGivenName(), userRepository.findUserByEmail(user.getEmail()).get().getGivenName());

        String samlResponse = samlResponse(magicLinkResponse);
        assertTrue(samlResponse.contains("new@example.com"));
    }

    @Test
    public void authenticationRequestExpired() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        doExpireWithFindProperty(SamlAuthenticationRequest.class, "id", authenticationRequestId);

        MagicLinkRequest linkRequest = new MagicLinkRequest(authenticationRequestId,
                new User("new@example.com", "Mary", "Doe"));

        magicLinkRequest(linkRequest, HttpMethod.POST)
                .response
                .statusCode(HttpStatus.GONE.value());
    }

    private MagicLinkResponse magicLinkRequest(User user, HttpMethod method) throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        return magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user), method);
    }

    private MagicLinkResponse magicLinkRequest(MagicLinkRequest linkRequest, HttpMethod method) {
        RequestSpecification requestSpecification = given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(linkRequest);

        Response response = method.equals(HttpMethod.POST) ? requestSpecification
                .post("/surfid/api/magic_link_request") : requestSpecification.put("/surfid/api/magic_link_request");
        return new MagicLinkResponse(linkRequest.getAuthenticationRequestId(), response.then());
    }

}
