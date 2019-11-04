package surfid.api;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;

public class UserControllerTest extends AbstractIntegrationTest {

    @Test
    public void existingUser() throws IOException {
        MagicLinkResponse magicLinkResponse = magicLinkRequest(new User("jdoe@example.com"), HttpMethod.PUT);
        magicLinkResponse
                .response
                .body("givenName", equalTo("John"))
                .body("familyName", equalTo("Doe"));
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();
        Response h = given().redirects().follow(false)
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .queryParam("h", samlAuthenticationRequest.getHash())
                .get("/saml/guest-idp/magic");
        System.out.println(h.getHeader("location"));
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
        magicLinkRequest(user, HttpMethod.POST)
                .response
                .body("givenName", equalTo("Mary"))
                .body("familyName", equalTo("Doe"));
        assertEquals(user.getGivenName(), userRepository.findUserByEmail(user.getEmail()).get().getGivenName());
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
