package myconext.api;

import io.restassured.common.mapper.TypeRef;
import myconext.AbstractIntegrationTest;
import myconext.exceptions.ForbiddenException;
import org.junit.Test;
import tiqr.org.model.Enrollment;
import tiqr.org.model.EnrollmentStatus;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TiqrControllerTest extends AbstractIntegrationTest {

    @Test
    public void start() {
        Map<String, String> body = given()
                .when()
                .get("/tiqrenroll/test")
                .body().as(new TypeRef<>() {
                });
        assertTrue(body.get("qr").startsWith("data:image/png;base64,"));
        String enrollmentKey = body.get("enrollmentKey");
        assertEquals(128, enrollmentKey.length());

        Enrollment enrollment = enrollmentRepository.findEnrollmentByKey(enrollmentKey).orElseThrow(ForbiddenException::new);
        assertEquals(EnrollmentStatus.INITIALIZED, enrollment.getStatus());
    }
}