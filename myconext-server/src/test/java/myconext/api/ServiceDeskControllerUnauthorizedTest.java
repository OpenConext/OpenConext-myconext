package myconext.api;

import myconext.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "service_desk_role_auto_provisioning=False"
        })
class ServiceDeskControllerUnauthorizedTest extends AbstractIntegrationTest {

    @Test
    void getUserControlCode() {
        given()
                .when()
                .get("/myconext/api/servicedesk/user/12345")
                .then()
                .statusCode(403);
    }
}