package myconext.remotecreation;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.EduID;
import myconext.model.IdpScoping;
import myconext.model.User;
import myconext.model.Verification;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RemoteCreationControllerTest extends AbstractIntegrationTest {

    private final String email = "jdoe@example.com";
    private final String userName = "studielink";
    private final String password = "secret";

    @Test
    void eduIDEmailExists() {
        Map<String, Object> result = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .queryParam("email", email)
                .get("/api/remote-creation/email-eduid-exists")
                .as(new TypeRef<>() {
                });
        assertEquals(200, result.get("status"));
    }

    @Test
    void eduIDEmailNotExists() {
        Map<String, Object> result = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .queryParam("email", "nope@nope.org")
                .get("/api/remote-creation/email-eduid-exists")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract()
                .as(new TypeRef<>() {
                });
        assertEquals(404, result.get("status"));
    }

    @Test
    void eduIDEExists() {
        Map<String, Object> result = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .queryParam("eduID", "fc75dcc7-6def-4054-b8ba-3c3cc504dd4b")
                .get("/api/remote-creation/eduid-exists")
                .as(new TypeRef<>() {
                });
        assertEquals(200, result.get("status"));
    }

    @Test
    void eduIDNotExists() {
        Map<String, Object> result = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .queryParam("eduID", UUID.randomUUID().toString())
                .get("/api/remote-creation/eduid-exists")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract()
                .as(new TypeRef<>() {
                });
        assertEquals(404, result.get("status"));
    }

    @Test
    void eduIDForInstitutionHappyFlow() {
        EduIDValue eduID = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(new EduIDInstitutionPseudonym("ST42", "fc75dcc7-6def-4054-b8ba-3c3cc504dd4b"))
                .post("/api/remote-creation/eduid-institution-pseudonym")
                .as(new TypeRef<>() {
                });
        User user = userRepository.findByEduIDS_value(eduID.getValue()).get();
        //See src/main/resources/manage/saml20_idp.json read by MockManage
        String institutionGUID = "880c1c27-9f3b-4f95-8a21-6b4f4118322b";
        EduID newEduID = user.getEduIDS().stream()
                .filter(anEduID -> anEduID.getServices().stream().anyMatch(service -> institutionGUID.equals(service.getInstitutionGuid())))
                .findFirst().get();
        assertEquals(eduID.getValue(), newEduID.getValue());
    }

    @Test
    void eduIDForInstitutionEduIDNotExists() {
        Map<String, Object> result = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(new EduIDInstitutionPseudonym("ST42", "nope"))
                .post("/api/remote-creation/eduid-institution-pseudonym")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract()
                .as(new TypeRef<>() {
                });
        assertEquals(404, result.get("status"));
    }

    @Test
    void eduIDForInstitutionBrinCodeNotExists() {
        Map<String, Object> result = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(new EduIDInstitutionPseudonym("nope", "fc75dcc7-6def-4054-b8ba-3c3cc504dd4b"))
                .post("/api/remote-creation/eduid-institution-pseudonym")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value())
                .extract()
                .as(new TypeRef<>() {
                });
        assertEquals(404, result.get("status"));
    }

    @Test
    void createEduIDHappyFlow() {
        StudieLinkEduID studieLinkEduID = new StudieLinkEduID(
                "new@user.com",
                null,
                "Mary",
                "Mary",
                "von",
                "Munich",
                "19880327",
                UUID.randomUUID().toString(),
                Verification.Decentraal,
                null
        );
        StudieLinkEduID studieLinkEduIdResult = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(studieLinkEduID)
                .post("/api/remote-creation/eduid-create")
                .as(new TypeRef<>() {
                });
        String eduIDValue = studieLinkEduIdResult.getEduIDValue();
        User user = userRepository.findByEduIDS_value(eduIDValue).get();
        EduID newEduID = user.getEduIDS().stream()
                .filter(anEduID -> anEduID.getValue().equals(eduIDValue))
                .findFirst().get();
        assertEquals(1, newEduID.getServices().size());
        //See src/main/resources/application.yml#external-api-configuration
        String institutionGUID = "ec9d6d75-0d11-e511-80d0-005056956c1a";
        assertEquals(institutionGUID, newEduID.getServices().get(0).getInstitutionGuid());

        assertEquals(1, user.getExternalLinkedAccounts().size());
        assertEquals(IdpScoping.studielink, user.getExternalLinkedAccounts().get(0).getIdpScoping());
    }

    @Test
    void createEduIDConflict() {
        StudieLinkEduID studieLinkEduID = new StudieLinkEduID(
                email,
                null,
                "Mary",
                "Mary",
                "von",
                "Munich",
                "19880327",
                UUID.randomUUID().toString(),
                Verification.Decentraal,
                null
        );
        given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(studieLinkEduID)
                .post("/api/remote-creation/eduid-create")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void createEduIDBadRequest() {
        StudieLinkEduID studieLinkEduID = new StudieLinkEduID(
                null,
                null,
                "Mary",
                "Mary",
                "von",
                "Munich",
                "19880327",
                UUID.randomUUID().toString(),
                Verification.Decentraal,
                null
        );
        given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(studieLinkEduID)
                .post("/api/remote-creation/eduid-create")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}