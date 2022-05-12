package myconext.tiqr;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import myconext.AbstractIntegrationTest;
import myconext.model.MagicLinkRequest;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import org.apache.commons.io.IOUtil;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tiqr.org.model.*;
import tiqr.org.secure.Challenge;
import tiqr.org.secure.OCRA;
import tiqr.org.secure.SecretCipher;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static myconext.security.GuestIdpAuthenticationRequestFilter.BROWSER_SESSION_COOKIE_NAME;
import static org.junit.jupiter.api.Assertions.*;

public class TiqrControllerTest extends AbstractIntegrationTest {

    @Test
    public void enrollmentFlowWithBackupCode() throws IOException {
        SamlAuthenticationRequest samlAuthenticationRequest = doEnrollmment();

        //We now should have a initialized registration and can request a backup code
        Map<String, String> results = given()
                .queryParam("hash", samlAuthenticationRequest.getHash())
                .get("/tiqr/generate-backup-code")
                .as(new TypeRef<>() {
                });
        assertEquals(9, results.get("recoveryCode").length());

        Registration registration = registrationRepository.findRegistrationByUserId(samlAuthenticationRequest.getUserId()).get();
        assertEquals(RegistrationStatus.FINALIZED, registration.getStatus());
    }

    @Test
    public void enrollmentFlowWithSMSVerification() throws IOException {
        SamlAuthenticationRequest samlAuthenticationRequest = doEnrollmment();

        //We now should have a initialized registration and can request a verification code
        given()
                .queryParam("hash", samlAuthenticationRequest.getHash())
                .body(Map.of("phoneNumber", "0612345678"))
                .contentType(ContentType.JSON)
                .post("/tiqr/send-phone-code")
                .then()
                .statusCode(200);

        User user = userRepository.findById(samlAuthenticationRequest.getUserId()).get();
        String phoneVerification = (String) user.getSurfSecureId().get(SURFSecureID.PHONE_VERIFICATION_CODE);

        given()
                .queryParam("hash", samlAuthenticationRequest.getHash())
                .body(Map.of("phoneVerification", phoneVerification))
                .contentType(ContentType.JSON)
                .post("/tiqr/verify-phone-code")
                .then()
                .statusCode(200);

        Registration registration = registrationRepository.findRegistrationByUserId(samlAuthenticationRequest.getUserId()).get();
        assertEquals(RegistrationStatus.FINALIZED, registration.getStatus());
    }

    @Test
    public void enrollmentFlowForSp() {
        Map<String, String> body = given()
                .when()
                .contentType(ContentType.JSON)
                .get("/tiqr/sp/start-enrollment")
                .body().as(new TypeRef<>() {
                });
        this.doFollowUpEnrollment(body);

    }

    @Test
    public void startAuthentication() throws IOException {
        SamlAuthenticationRequest samlAuthenticationRequest = doEnrollmment();
        //Fake registration
        Registration registration = registrationRepository.findRegistrationByUserId(samlAuthenticationRequest.getUserId()).get();
        registration.setStatus(RegistrationStatus.FINALIZED);
        registrationRepository.saveAll(Arrays.asList(registration));

        Map<String, Object> results = given()
                .body(new TiqrRequest(samlAuthenticationRequest.getId(), "jdoe@example.com"))
                .contentType(ContentType.JSON)
                .post("/tiqr/start-authentication")
                .as(new TypeRef<>() {
                });
        assertNotNull(results.get("qr"));
        assertFalse((Boolean) results.get("tiqrCookiePresent"));

        String sessionKey = (String) results.get("sessionKey");
        Map<String, String> status = given()
                .queryParam("sessionKey", sessionKey)
                .queryParam("id", samlAuthenticationRequest.getId())
                .get("/tiqr/poll-authentication")
                .as(new TypeRef<>() {
                });
        assertEquals(AuthenticationStatus.PENDING.name(), status.get("status"));

        //mock the call from the device to the TiqrController
        SecretCipher secretCipher = new SecretCipher("secret");
        Authentication authentication = authenticationRepository.findAuthenticationBySessionKey(sessionKey).get();
        registration = registrationRepository.findById(registration.getId()).get();
        String decryptedSecret = secretCipher.decrypt(registration.getSecret());
        String ocra = OCRA.generateOCRA(decryptedSecret, authentication.getChallenge(), sessionKey);

        given()
                .contentType(ContentType.URLENC)
                .formParam("sessionKey", sessionKey)
                .formParam("userId", samlAuthenticationRequest.getUserId())
                .formParam("response", ocra)
                .formParam("language", "en")
                .formParam("operation", "login")
                .formParam("notificationType", "APNS")
                .formParam("notificationAddress", "1234567890")
                .post("/tiqr/authentication");

        Map<String, String> newStatus = given()
                .queryParam("sessionKey", sessionKey)
                .queryParam("id", samlAuthenticationRequest.getId())
                .get("/tiqr/poll-authentication")
                .as(new TypeRef<>() {
                });
        assertEquals(AuthenticationStatus.SUCCESS.name(), newStatus.get("status"));

        String hash = newStatus.get("hash");
        given()
                .body(Map.of("hash", hash))
                .contentType(ContentType.JSON)
                .put("/tiqr/remember-me")
                .then()
                .statusCode(200);
        //First one is the rememberme
        given().redirects().follow(false)
                .when()
                .queryParam("h", hash)
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .get("/saml/guest-idp/magic");
        //second is the actual SAML
        Response response = given().redirects().follow(false)
                .when()
                .queryParam("h", hash)
                .cookie(BROWSER_SESSION_COOKIE_NAME, "true")
                .get("/saml/guest-idp/magic");
        Headers headers = response.getHeaders();
        List<String> cookies = headers.getValues("Set-cookie");
        List.of("TIQR_COOKIE=true", "BROWSER_SESSION=true", "TRACKING_DEVICE=", "SESSION=", "guest-idp-remember-me=")
                .forEach(s -> assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith(s))));

        String html = IOUtil.toString(response.asInputStream());
        Matcher matcher = Pattern.compile("name=\"SAMLResponse\" value=\"(.*?)\"").matcher(html);
        matcher.find();

        String saml = new String(Base64.getDecoder().decode(matcher.group(1)));
        assertTrue(saml.contains("jdoe@example.com"));
    }

    private SamlAuthenticationRequest doEnrollmment() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        User user = user("jdoe@example.com");
        MagicLinkRequest magicLinkRequest = new MagicLinkRequest(authenticationRequestId, user, false, false);
        magicLinkRequest(magicLinkRequest, HttpMethod.PUT);

        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId).get();
        Map<String, String> body = given()
                .queryParam("hash", samlAuthenticationRequest.getHash())
                .when()
                .contentType(ContentType.JSON)
                .get("/tiqr/start-enrollment")
                .body().as(new TypeRef<>() {
                });
        doFollowUpEnrollment(body);
        return samlAuthenticationRequest;
    }

    private void doFollowUpEnrollment(Map<String, String> body) {
        String enrollmentKey = body.get("enrollmentKey");
        String metaDataUrl = String.format("http://localhost:8081/tiqr/metadata?enrollment_key=%s", enrollmentKey);
        String url = String.format("https://eduid.nl/tiqrenroll?metadata=%s", URLEncoder.encode(metaDataUrl, Charset.defaultCharset()));
        assertEquals(url, body.get("url"));
        assertTrue(body.get("qrcode").startsWith("data:image/png;base64,"));

        String enrollmentStatus = given()
                .queryParam("enrollmentKey", enrollmentKey)
                .get("/tiqr/poll-enrollment")
                .as(String.class);
        assertEquals(EnrollmentStatus.INITIALIZED.name(), enrollmentStatus);

        MetaData metaData = given()
                .queryParam("enrollment_key", enrollmentKey)
                .get("/tiqr/metadata")
                .as(MetaData.class);
        assertEquals("John Doe", metaData.getIdentity().getDisplayName());

        enrollmentStatus = given()
                .queryParam("enrollmentKey", enrollmentKey)
                .get("/tiqr/poll-enrollment")
                .as(String.class);
        assertEquals(EnrollmentStatus.RETRIEVED.name(), enrollmentStatus);

        UriComponents uriComponents = UriComponentsBuilder.fromUriString(metaData.getService().getEnrollmentUrl()).build();
        String enrollmentSecret = uriComponents.getQueryParams().getFirst("enrollment_secret");

        String sessionKey = Challenge.generateSessionKey();
        given()
                .queryParam("enrollment_secret", enrollmentSecret)
                .contentType(ContentType.URLENC)
                .formParam("userid", metaData.getIdentity().getIdentifier())
                .formParam("secret", sessionKey)
                .formParam("language", "en")
                .formParam("notificationType", "APNS")
                .formParam("notificationAddress", "1234567890")
                .formParam("version", "2")
                .formParam("operation", "register")
                .post("/tiqr/enrollment");

        enrollmentStatus = given()
                .queryParam("enrollmentKey", enrollmentKey)
                .get("/tiqr/poll-enrollment")
                .as(String.class);
        assertEquals(EnrollmentStatus.PROCESSED.name(), enrollmentStatus);
    }
}