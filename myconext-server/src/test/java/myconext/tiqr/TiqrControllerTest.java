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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

public class TiqrControllerTest extends AbstractIntegrationTest {

    @Test
    public void enrollmentFlowWithBackupCode() throws IOException {
        SamlAuthenticationRequest samlAuthenticationRequest = doEnrollmment(false);

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
        SamlAuthenticationRequest samlAuthenticationRequest = doEnrollmment(false);

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
    public void forbiddenEnrollment() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .get("/tiqr/start-enrollment")
                .then()
                .statusCode(403);
    }

    @Test
    public void spBackupCode() throws IOException {
        doEnrollmment(false);
        Map<String, String> body = given()
                .when()
                .contentType(ContentType.JSON)
                .get("/tiqr/sp/generate-backup-code")
                .body().as(new TypeRef<>() {
                });
        assertTrue(body.containsKey("recoveryCode"));
    }

    @Test
    public void spSendPhoneCode() throws IOException {
        doEnrollmment(true);

        String phoneNumber = "0612345678";
        given()
                .when()
                .contentType(ContentType.JSON)
                .body(Map.of("phoneNumber", phoneNumber))
                .post("/tiqr/sp/send-phone-code")
                .body().as(new TypeRef<>() {
                });
        User user = userRepository.findUserByEmail("jdoe@example.com").get();
        assertEquals(phoneNumber, user.getSurfSecureId().get(SURFSecureID.PHONE_NUMBER));
    }

    @Test
    public void spVerifyPhoneCode() throws IOException {
        doEnrollmment(true);

        String phoneVerification = "0612345678";

        User user = userRepository.findUserByEmail("jdoe@example.com").get();
        user.getSurfSecureId().put(SURFSecureID.PHONE_VERIFICATION_CODE, phoneVerification);
        userRepository.save(user);

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(Map.of("phoneVerification", phoneVerification))
                .post("/tiqr/sp/verify-phone-code")
                .then()
                .statusCode(200);
        user = userRepository.findUserByEmail("jdoe@example.com").get();
        assertTrue((Boolean) user.getSurfSecureId().get(SURFSecureID.PHONE_VERIFIED));
    }

    @Test
    public void spVerifyPhoneCodeWrongCode() {
        String phoneVerification = "0612345678";

        User user = userRepository.findUserByEmail("jdoe@example.com").get();
        user.getSurfSecureId().put(SURFSecureID.PHONE_VERIFICATION_CODE, phoneVerification);
        userRepository.save(user);

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(Map.of("phoneVerification", "nope"))
                .post("/tiqr/sp/verify-phone-code")
                .then()
                .statusCode(403);
    }

    @Test
    public void spStartAuthentication() throws IOException {
        doEnrollmment(true);

        Map<String, String> body = given()
                .when()
                .contentType(ContentType.JSON)
                .post("/tiqr/sp/start-authentication")
                .body().as(new TypeRef<>() {
                });
        assertTrue(body.containsKey("sessionKey"));
    }

    @Test
    public void spDeactivationCode() {
        User user = userRepository.findUserByEmail("jdoe@example.com").get();
        user.getSurfSecureId().put(SURFSecureID.PHONE_NUMBER, "0612345678");
        user.getSurfSecureId().put(SURFSecureID.RATE_LIMIT, 2);
        userRepository.save(user);

        given()
                .when()
                .contentType(ContentType.JSON)
                .get("/tiqr/sp/send-deactivation-phone-code")
                .then()
                .statusCode(200);

        user = userRepository.findUserByEmail("jdoe@example.com").get();
        assertFalse(user.getSurfSecureId().containsKey(SURFSecureID.RATE_LIMIT));
        assertTrue(user.getSurfSecureId().containsKey(SURFSecureID.PHONE_VERIFICATION_CODE));
    }

    @Test
    public void spDeactivationCodeForbidden() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .get("/tiqr/sp/send-deactivation-phone-code")
                .then()
                .statusCode(403);
    }

    @Test
    public void spDeactivation() throws IOException {
        doEnrollmment(false);

        User user = userRepository.findUserByEmail("jdoe@example.com").get();
        user.getSurfSecureId().put(SURFSecureID.RECOVERY_CODE, "123456");
        user.getSurfSecureId().put(SURFSecureID.RATE_LIMIT, 2);
        userRepository.save(user);

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(Map.of("verificationCode", "123456"))
                .post("/tiqr/sp/deactivate-app")
                .then()
                .statusCode(200);
        user = userRepository.findUserByEmail("jdoe@example.com").get();
        assertTrue(user.getSurfSecureId().isEmpty());
    }

    @Test
    public void spDeactivationNoVerificationStep() throws IOException {
        doEnrollmment(false);

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(Map.of("verificationCode", "123456"))
                .post("/tiqr/sp/deactivate-app")
                .then()
                .statusCode(400);
    }

    @Test
    public void spDeactivationWrongCode() throws IOException {
        doEnrollmment(false);

        User user = userRepository.findUserByEmail("jdoe@example.com").get();
        user.getSurfSecureId().put(SURFSecureID.RECOVERY_CODE, "123456");
        userRepository.save(user);

        given()
                .when()
                .contentType(ContentType.JSON)
                .body(Map.of("verificationCode", "nope"))
                .post("/tiqr/sp/deactivate-app")
                .then()
                .statusCode(403);
    }

    @Test
    public void fetchRegistration() throws IOException {
        doEnrollmment(true);
        Map<String, Object> results = given()
                .when()
                .get("/myconext/api/sp/me")
                .as(new TypeRef<>() {
                });
        Map<String, Object> registration = (Map<String, Object>) results.get("registration");
        assertEquals(RegistrationStatus.FINALIZED.name(), registration.get("status"));
    }

    @Test
    public void startAuthentication() throws IOException {
        SamlAuthenticationRequest samlAuthenticationRequest = doEnrollmment(true);

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

        Registration registration = registrationRepository.findRegistrationByUserId(samlAuthenticationRequest.getUserId()).get();
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
                .post("/tiqr/authentication")
                .then()
                .statusCode(200);

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

    @Test
    public void manualAuthentication() throws IOException {
        SamlAuthenticationRequest samlAuthenticationRequest = doEnrollmment(true);

        Map<String, Object> results = given()
                .body(new TiqrRequest(samlAuthenticationRequest.getId(), "jdoe@example.com"))
                .contentType(ContentType.JSON)
                .post("/tiqr/start-authentication")
                .as(new TypeRef<>() {
                });
        String sessionKey = (String) results.get("sessionKey");
        //mock the call from the device to the TiqrController
        SecretCipher secretCipher = new SecretCipher("secret");
        Authentication authentication = authenticationRepository.findAuthenticationBySessionKey(sessionKey).get();

        Registration registration = registrationRepository.findRegistrationByUserId(samlAuthenticationRequest.getUserId()).get();
        registration = registrationRepository.findById(registration.getId()).get();
        String decryptedSecret = secretCipher.decrypt(registration.getSecret());
        String ocra = OCRA.generateOCRA(decryptedSecret, authentication.getChallenge(), sessionKey);

        given()
                .contentType(ContentType.JSON)
                .body(Map.of("sessionKey", sessionKey, "response", ocra))
                .post("/tiqr/manual-response")
                .then()
                .statusCode(200);

        Map<String, String> newStatus = given()
                .queryParam("sessionKey", sessionKey)
                .queryParam("id", samlAuthenticationRequest.getId())
                .get("/tiqr/poll-authentication")
                .as(new TypeRef<>() {
                });
        assertEquals(AuthenticationStatus.SUCCESS.name(), newStatus.get("status"));
    }

    private SamlAuthenticationRequest doEnrollmment(boolean finishRegistration) throws IOException {
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
        if (finishRegistration) {
            //Fake registration
            Registration registration = registrationRepository.findRegistrationByUserId(samlAuthenticationRequest.getUserId()).get();
            registration.setStatus(RegistrationStatus.FINALIZED);
            registrationRepository.saveAll(Arrays.asList(registration));
        }
        return samlAuthenticationRequest;
    }

    private void doFollowUpEnrollment(Map<String, String> body) {
        String enrollmentKey = body.get("enrollmentKey");
        String metaDataUrl = String.format("http://localhost:8081/tiqr/metadata?enrollment_key=%s", enrollmentKey);
        String url = String.format("https://eduid.nl/tiqrenroll/?metadata=%s", URLEncoder.encode(metaDataUrl, Charset.defaultCharset()));
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
                .post("/tiqr/enrollment")
                .then()
                .statusCode(200);

        enrollmentStatus = given()
                .queryParam("enrollmentKey", enrollmentKey)
                .get("/tiqr/poll-enrollment")
                .as(String.class);
        assertEquals(EnrollmentStatus.PROCESSED.name(), enrollmentStatus);
    }
}
