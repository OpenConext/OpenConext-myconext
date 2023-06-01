package myconext.api;

import io.restassured.response.Response;
import myconext.AbstractIntegrationTest;
import myconext.model.MagicLinkRequest;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static myconext.security.GuestIdpAuthenticationRequestFilter.BROWSER_SESSION_COOKIE_NAME;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "mongodb_db=surf_id_test",
                "cron.node-cron-job-responsible=false",
                "email_guessing_sleep_millis=1",
                "sp_entity_id=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "sp_entity_metadata_url=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "spring.main.lazy-initialization=true",
                "eduid_api.oidcng_introspection_uri=http://localhost:8098/introspect",
                "cron.service-name-resolver-initial-delay-milliseconds=60000",
                "sso_mfa_duration_seconds=3600"
        })
@ActiveProfiles(value = {"test"}, inheritProfiles = false)
@SuppressWarnings("unchecked")
public class UserControllerSSOMfaTest extends AbstractIntegrationTest {

    @Test
    public void mfaSSOTiqrAuthentication() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        String authnContext = readFile("request_authn_context_mfa.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);
        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false, false), HttpMethod.PUT);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();
        samlAuthenticationRequest.setTiqrFlow(true);
        authenticationRequestRepository.save(samlAuthenticationRequest);

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

        assertTrue(samlResponse.contains("<saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/>"));
        assertTrue(samlResponse.contains("https://refeds.org/profile/mfa"));
    }

    @Test
    public void mfaSSOTiqrAuthenticated() throws IOException {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        String authnContext = readFile("request_authn_context_mfa.xml");
        Response response = samlAuthnRequestResponseWithLoa(null, "relay", authnContext);
        String authenticationRequestId = extractAuthenticationRequestIdFromAuthnResponse(response);

        MagicLinkResponse magicLinkResponse = magicLinkRequest(new MagicLinkRequest(authenticationRequestId, user, false, false), HttpMethod.PUT);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(magicLinkResponse.authenticationRequestId).get();
        samlAuthenticationRequest.setTiqrFlow(true);
        authenticationRequestRepository.save(samlAuthenticationRequest);

        String samlResponse = samlResponse(magicLinkResponse);
        assertTrue(samlResponse.contains("<saml2p:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/>"));
        assertTrue(samlResponse.contains("https://refeds.org/profile/mfa"));
    }

}
