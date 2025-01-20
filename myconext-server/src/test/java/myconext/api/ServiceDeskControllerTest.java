package myconext.api;

import myconext.AbstractIntegrationTest;
import myconext.model.ControlCode;
import myconext.model.ExternalLinkedAccount;
import myconext.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "service_desk_role_auto_provisioning=True"
        })
class ServiceDeskControllerTest extends AbstractIntegrationTest {

    @Test
    void getUserControlCode() {
        ControlCode controlCode = given()
                .when()
                .pathParam("code", "12345")
                .get("/myconext/api/servicedesk/user/{code}")
                .as(ControlCode.class);
        assertEquals("12345", controlCode.getCode());
        assertEquals("mdoe", controlCode.getUserUid());
    }

    @Test
    void getUserControlCodeNotFound() {
        given()
                .when()
                .pathParam("code", "Nope")
                .get("/myconext/api/servicedesk/user/{code}")
                .then()
                .statusCode(404);
    }

    @Test
    void validateDate() {
        Boolean valid = given()
                .when()
                .queryParam("dayOfBirth", "16 Mar 1981")
                .get("/myconext/api/servicedesk/validate")
                .as(Boolean.class);
        assertTrue(valid);
    }

    @Test
    void validateInvalidDate() {
        Boolean valid = given()
                .when()
                .queryParam("dayOfBirth", "Nope")
                .get("/myconext/api/servicedesk/validate")
                .as(Boolean.class);
        assertFalse(valid);
    }


    @Test
    void convertUserControlCode() {
        ControlCode controlCode = given()
                .when()
                .pathParam("code", "12345")
                .get("/myconext/api/servicedesk/user/{code}")
                .as(ControlCode.class);

        given()
                .when()
                .body(controlCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .put("/myconext/api/servicedesk/approve")
                .then()
                .statusCode(201);

        User user = userRepository.findOneUserByEmail("mdoe@example.com");
        assertEquals(1, user.getExternalLinkedAccounts().size());
        assertNull(user.getControlCode());

        ExternalLinkedAccount externalLinkedAccount = user.getExternalLinkedAccounts().getFirst();
        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy").toFormatter();
        ZonedDateTime dateOfBirth = externalLinkedAccount.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault());
        //See myconext-server/src/test/resources/users.json
        assertEquals("11-03-1987", dateTimeFormatter.format(dateOfBirth));
    }

    @Test
    void convertUserControlCodeForbidden() {
        ControlCode controlCode = new ControlCode("firstName", "lastName", "dayOfBirth");
        controlCode.setCode("12345");
        controlCode.setUserUid("bogus");

        given()
                .when()
                .body(controlCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .put("/myconext/api/servicedesk/approve")
                .then()
                .statusCode(403);
    }

    @Test
    void convertUserControlCodeLinkedAccountsPresent() {
        ControlCode controlCode = new ControlCode("firstName", "lastName", "dayOfBirth");
        controlCode.setCode("54321");

        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.setControlCode(controlCode);
        userRepository.save(user);

        given()
                .when()
                .body(controlCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .put("/myconext/api/servicedesk/approve")
                .then()
                .statusCode(403);
    }
}