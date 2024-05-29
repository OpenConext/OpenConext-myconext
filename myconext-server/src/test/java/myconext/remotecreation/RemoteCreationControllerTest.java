package myconext.remotecreation;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RemoteCreationControllerTest extends AbstractIntegrationTest {

    private final String userName = "studielink";
    private final String password = "secret";

    @Test
    void remoteCreation() {
        Map<String, Object> result = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .get("/api/remote-creation/eduid-exists")
                .as(new TypeRef<>() {
                });
        assertEquals("todo", result.get("status"));
    }
}