package myconext.api;

import myconext.AbstractIntegrationTest;
import myconext.model.ControlCode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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

    @org.junit.Test
    public void createUserControlCodeForbidden() {
        ControlCode controlCode = new ControlCode("Lee", "Harpers", "01 Mar 1977");
        given()
                .body(controlCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/myconext/api/sp/control-code")
                .then()
                .statusCode(403);
    }

}