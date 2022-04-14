package myconext.tiqr;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.MagicLinkRequest;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tiqr.org.model.*;
import tiqr.org.secure.Challenge;

import java.io.IOException;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                .body(Map.of("phoneNumber","0612345678"))
                .contentType(ContentType.JSON)
                .post("/tiqr/send-phone-code")
                .then()
                        .statusCode(200);

        User user = userRepository.findById(samlAuthenticationRequest.getUserId()).get();
        String phoneVerification = (String) user.getSurfSecureId().get(SURFSecureID.PHONE_VERIFICATION_CODE);

        given()
                .queryParam("hash", samlAuthenticationRequest.getHash())
                .body(Map.of("phoneVerification",phoneVerification))
                .contentType(ContentType.JSON)
                .post("/tiqr/verify-phone-code")
                .then()
                .statusCode(200);

        Registration registration = registrationRepository.findRegistrationByUserId(samlAuthenticationRequest.getUserId()).get();
        assertEquals(RegistrationStatus.FINALIZED, registration.getStatus());
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
        String enrollmentKey = body.get("enrollmentKey");
        String url = String.format("http://localhost:8081/tiqr/metadata?enrollment_key=%s)", enrollmentKey);

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
                .formParam("userid", samlAuthenticationRequest.getUserId())
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
        return samlAuthenticationRequest;
    }
}