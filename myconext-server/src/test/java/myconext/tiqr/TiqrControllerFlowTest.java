package myconext.tiqr;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.common.mapper.TypeRef;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import myconext.AbstractIntegrationTest;
import myconext.model.MagicLinkRequest;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import tiqr.org.model.*;
import tiqr.org.secure.Challenge;
import tiqr.org.secure.OCRA;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static myconext.security.GuestIdpAuthenticationRequestFilter.BROWSER_SESSION_COOKIE_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unchecked")
public class TiqrControllerFlowTest extends AbstractIntegrationTest {

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8098);

    @Test
    public void tiqrMobileFlow() throws Exception {
        Map<String, String> body = given()
                .redirects().follow(false)
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .get("/mobile/tiqr/sp/start-enrollment")
                .body().as(new TypeRef<>() {
                });

        String enrollmentKey = body.get("enrollmentKey");
        String metaDataUrl = String.format("http://localhost:8081/tiqr/metadata?enrollment_key=%s", enrollmentKey);
        String url = String.format("https://eduid.nl/tiqrenroll/?metadata=%s", URLEncoder.encode(metaDataUrl, Charset.defaultCharset()));
        assertEquals(url, body.get("url"));
        assertTrue(body.get("qrcode").startsWith("data:image/png;base64,"));

        String enrollmentStatus = given()
                .queryParam("enrollmentKey", enrollmentKey)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .get("/mobile/tiqr/poll-enrollment")
                .as(String.class);
        assertEquals(EnrollmentStatus.INITIALIZED.name(), enrollmentStatus);

        MetaData metaData = given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .queryParam("enrollment_key", enrollmentKey)
                .get("/mobile/tiqr/metadata")
                .as(MetaData.class);
        assertEquals("John Doe", metaData.getIdentity().getDisplayName());

        enrollmentStatus = given()
                .queryParam("enrollmentKey", enrollmentKey)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .get("/mobile/tiqr/poll-enrollment")
                .as(String.class);
        assertEquals(EnrollmentStatus.RETRIEVED.name(), enrollmentStatus);

        UriComponents uriComponents = UriComponentsBuilder.fromUriString(metaData.getService().getEnrollmentUrl()).build();
        String enrollmentSecret = uriComponents.getQueryParams().getFirst("enrollment_secret");

        String sessionKey = Challenge.generateSessionKey();
        given()
                .queryParam("enrollment_secret", enrollmentSecret)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .contentType(ContentType.URLENC)
                .formParam("userid", metaData.getIdentity().getIdentifier())
                .formParam("secret", sessionKey)
                .formParam("language", "en")
                .formParam("notificationType", "APNS")
                .formParam("notificationAddress", "1234567890")
                .formParam("version", "2")
                .formParam("operation", "register")
                .post("/mobile/tiqr/enrollment")
                .then()
                .statusCode(200);

        enrollmentStatus = given()
                .queryParam("enrollmentKey", enrollmentKey)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .get("/mobile/tiqr/poll-enrollment")
                .as(String.class);
        assertEquals(EnrollmentStatus.PROCESSED.name(), enrollmentStatus);

        String phoneNumber = "0612345678";
        given()
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .body(Map.of("phoneNumber", phoneNumber))
                .post("/mobile/tiqr/sp/send-phone-code")
                .body().as(new TypeRef<>() {
                });

        User user = userRepository.findUserByEmail("jdoe@example.com").get();
        String phoneVerification = (String) user.getSurfSecureId().get(SURFSecureID.PHONE_VERIFICATION_CODE);

        given()
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .body(Map.of("phoneVerification", phoneVerification))
                .post("/mobile/tiqr/sp/verify-phone-code")
                .body().as(new TypeRef<>() {
                });

        Map<String, String> startAuthentication = given()
                .when()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .post("/mobile/tiqr/sp/start-authentication")
                .body().as(new TypeRef<>() {
                });
        String generatedSessionKey = startAuthentication.get("sessionKey");

        Registration registration = registrationRepository.findRegistrationByUserId(user.getId()).get();
        String decryptedSecret = this.decryptRegistrationSecret(registration.getSecret());
        Authentication authentication = authenticationRepository.findAuthenticationBySessionKey(generatedSessionKey).get();

        String ocra = OCRA.generateOCRA(decryptedSecret, authentication.getChallenge(), generatedSessionKey);

        given()
                .contentType(ContentType.URLENC)
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .formParam("sessionKey", generatedSessionKey)
                .formParam("userId", metaData.getIdentity().getIdentifier())
                .formParam("response", ocra)
                .formParam("language", "en")
                .formParam("operation", "login")
                .formParam("notificationType", "APNS")
                .formParam("notificationAddress", "1234567890")
                .post("/mobile/tiqr/authentication")
                .then()
                .statusCode(200)
                .body(equalTo("OK"));

        Map<String, String> newStatus = given()
                .queryParam("sessionKey", generatedSessionKey)
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .get("/mobile/tiqr/sp/poll-authentication")
                .as(new TypeRef<>() {
                });
        assertEquals(AuthenticationStatus.SUCCESS.name(), newStatus.get("status"));

    }


}
