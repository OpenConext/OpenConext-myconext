package myconext.api;

import io.restassured.response.Response;
import myconext.AbstractIntegrationTest;
import myconext.model.ClientAuthenticationRequest;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;

import java.io.IOException;

import static io.restassured.RestAssured.given;
import static myconext.security.GuestIdpAuthenticationRequestFilter.BROWSER_SESSION_COOKIE_NAME;
import static org.junit.Assert.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "feature.use_app=false"
    })
public class UserControllerUseAppDisabledTest extends AbstractIntegrationTest {

    @Test
    public void nudgeUserToUseTheApp_skip() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.getSurfSecureId().clear();
        userRepository.save(user);

        String authnContext = readFile("request_authn_context_validated_name.xml");
        Response magicLinkResponse = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(magicLinkResponse);

        ClientAuthenticationResponse authenticationResponse = oneTimeLoginCodeRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false, "repsonse"), HttpMethod.PUT);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationResponse.authenticationRequestId).get();
        Response confirmResponse = given().redirects().follow(false)
                .when()
                .queryParam("h", samlAuthenticationRequest.getHash())
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .get("/saml/guest-idp/magic");
        String location = confirmResponse.getHeader("Location");
        assertTrue(location.startsWith("http://localhost:3000/remember?h="));
    }
}

