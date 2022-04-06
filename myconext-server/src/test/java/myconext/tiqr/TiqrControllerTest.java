package myconext.tiqr;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.MagicLinkRequest;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.http.HttpMethod;

import java.io.IOException;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TiqrControllerTest extends AbstractIntegrationTest {

    @Test
    public void startEnrollment() throws IOException {
        String authenticationRequestId = samlAuthnRequest();
        User user = user("jdoe@example.com");
        MagicLinkRequest magicLinkRequest = new MagicLinkRequest(authenticationRequestId, user, false, false);
        magicLinkRequest(magicLinkRequest, HttpMethod.PUT);

        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId).get();
        Map<String, String> body = given()
                .queryParam("hash", samlAuthenticationRequest.getHash())
                .when()
                .contentType(ContentType.JSON)
                .get("/tiqr/start")
                .body().as(new TypeRef<>() {
                });
        String enrollmentKey = body.get("enrollmentKey");
        String url = String.format("http://localhost:8081/tiqr/metadata?enrollment_key=%s)", enrollmentKey);

        assertEquals(url, body.get("url"));
        assertTrue(body.get("qrcode").startsWith("data:image/png;base64,"));
    }
}