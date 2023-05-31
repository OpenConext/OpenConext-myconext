package myconext.api;

import com.yubico.webauthn.data.*;
import com.yubico.webauthn.data.exception.Base64UrlException;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.Filter;
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
import org.apache.commons.io.IOUtil;
import org.apache.http.client.CookieStore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
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
@ActiveProfiles(value = {"test"},inheritProfiles = false)
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
    }

}
