package myconext.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.exceptions.ForbiddenException;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import tiqr.org.model.Enrollment;
import tiqr.org.model.EnrollmentStatus;
import tiqr.org.secure.Challenge;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles(value = "prod", inheritProfiles = false)
public class TiqrControllerTest extends AbstractIntegrationTest {

    @Test
    public void qrCode() {
        Map<String, String> body = given()
                .when()
                .contentType(ContentType.JSON)
                .body(Collections.singletonMap("url", String.format("https://%s@eduid.nl/tiqrauth/%s/%s/%s/1",
                        UUID.randomUUID(),
                        Challenge.generateSessionKey(),
                        Challenge.generateQH10Challenge(),
                        Challenge.generateNonce())))
                .post("/tiqr/qrcode")
                .body().as(new TypeRef<>() {
                });
        assertTrue(body.get("qrcode").startsWith("data:image/png;base64,"));
    }
}