package myconext.api;

import io.restassured.http.Cookie;
import io.restassured.response.Response;
import myconext.AbstractIntegrationTest;
import myconext.model.ClientAuthenticationRequest;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import myconext.tiqr.SURFSecureID;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.Optional;

import static myconext.security.GuestIdpAuthenticationRequestFilter.GUEST_IDP_REMEMBER_ME_COOKIE_NAME;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
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
                "oidc.base-url=http://localhost:8098/",
                "sso_mfa_duration_seconds=-1000",
                "feature.default_remember_me=True"
        })
public class UserControllerRememberMeTest extends AbstractIntegrationTest {

    @Test
    public void rememberMe() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        User user = user("steve@example.com", "Steve", "Doe", "en");
        ClientAuthenticationResponse magicLinkResponse = magicLinkRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.POST);
        Response response = magicResponse(magicLinkResponse);

        String cookie = response.cookie(GUEST_IDP_REMEMBER_ME_COOKIE_NAME);
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository
                .findByRememberMeValue(cookie)
                .orElseThrow(IllegalArgumentException::new);

        assertEquals(true, samlAuthenticationRequest.isRememberMe());

        String saml = samlAuthnResponse(samlAuthnRequestResponse(new Cookie.Builder(GUEST_IDP_REMEMBER_ME_COOKIE_NAME, cookie).build(), null), Optional.empty());
        assertTrue(saml.contains("steve@example.com<"));

        user = userRepository.findOneUserByEmail("steve@example.com");
        long count = authenticationRequestRepository.deleteByUserId(user.getId());
        assertEquals(1, count);
    }

    @Test
    public void rememberMeButAccountLinkingRequired() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        User user = user("steve@example.com", "Steve", "Doe", "en");
        ClientAuthenticationResponse magicLinkResponse = magicLinkRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.POST);
        Response response = magicResponse(magicLinkResponse);

        String cookie = response.cookie(GUEST_IDP_REMEMBER_ME_COOKIE_NAME);
        String authnContext = readFile("request_authn_context.xml");
        response = samlAuthnRequestResponseWithLoa(
                new Cookie.Builder(GUEST_IDP_REMEMBER_ME_COOKIE_NAME, cookie).build(), null, authnContext);
        response.then().header("Location", startsWith("http://localhost:3000/stepup/"));
    }

    @Test
    public void rememberMeButMFARegistrationRequired() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        User user = user("steve@example.com", "Steve", "Doe", "en");
        ClientAuthenticationResponse magicLinkResponse = magicLinkRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.POST);
        Response response = magicResponse(magicLinkResponse);

        String cookie = response.cookie(GUEST_IDP_REMEMBER_ME_COOKIE_NAME);
        String authnContext = readFile("request_authn_context_mfa.xml");
        response = samlAuthnRequestResponseWithLoa(
                new Cookie.Builder(GUEST_IDP_REMEMBER_ME_COOKIE_NAME, cookie).build(), null, authnContext);
        response.then().header("Location", startsWith("http://localhost:3000/uselink/"));
    }

    @Test
    public void rememberMeButMFALoginRequired() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        User user = user("steve@example.com", "Steve", "Doe", "en");
        user.getSurfSecureId().put(SURFSecureID.RECOVERY_CODE, "12345678");
        user.setNewUser(false);
        userRepository.save(user);

        ClientAuthenticationResponse magicLinkResponse = magicLinkRequest(new ClientAuthenticationRequest(authenticationRequestId, user, false), HttpMethod.PUT);
        Response response = magicResponse(magicLinkResponse);

        String cookie = response.cookie(GUEST_IDP_REMEMBER_ME_COOKIE_NAME);
        String authnContext = readFile("request_authn_context_mfa.xml");
        response = samlAuthnRequestResponseWithLoa(
                new Cookie.Builder(GUEST_IDP_REMEMBER_ME_COOKIE_NAME, cookie).build(), null, authnContext);
        response.then().header("Location", startsWith("http://localhost:3000/useapp/"));
    }


}
