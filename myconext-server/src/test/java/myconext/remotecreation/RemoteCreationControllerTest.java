package myconext.remotecreation;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SuppressWarnings("unchecked")
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
                //See src/test/resources/users.json eduIDs#value
                .body(new EduIDInstitutionPseudonym("ST42", "fc75dcc7-6def-4054-b8ba-3c3cc504dd4b"))
                .post("/api/remote-creation/eduid-institution-pseudonym")
                .as(new TypeRef<>() {
                });
        User user = this.findUserByEduIDValue(eduID.getValue()).get();
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
        ExternalEduID externalEduID = new ExternalEduID(
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
        ExternalEduID externalEduIDResult = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduID)
                .post("/api/remote-creation/eduid-create")
                .as(new TypeRef<>() {
                });
        String eduIDValue = externalEduIDResult.getEduIDValue();
        User user = this.findUserByEduIDValue(eduIDValue).get();
        assertFalse(user.isNewUser());

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
        ExternalEduID externalEduID = new ExternalEduID(
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
                .body(externalEduID)
                .post("/api/remote-creation/eduid-create")
                .then()
                .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void createEduIDBadRequest() {
        ExternalEduID externalEduID = new ExternalEduID(
                null,
                null,
                "",
                "Mary",
                "von",
                "",
                "19880327",
                "",
                null,
                null
        );
        Map<String, Object> errorResult = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduID)
                .post("/api/remote-creation/eduid-create")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract()
                .as(new TypeRef<>() {
                });
        assertEquals(Stream.of("chosenName", "identifier", "lastName", "verification").sorted().collect(Collectors.toList()),
                ((List<Map<String, Object>>) errorResult.get("errors")).stream().map(m -> m.get("field")).sorted().collect(Collectors.toList()));
    }

    @Test
    void createEduIDValidationException() {
        ExternalEduID externalEduID = new ExternalEduID(
                "new@eduid.com",
                null,
                "Peter",
                "",
                null,
                "Pip",
                "",
                UUID.randomUUID().toString(),
                Verification.Geverifieerd,
                null
        );
        Map<String, Object> errorResult = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduID)
                .post("/api/remote-creation/eduid-create")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .extract()
                .as(new TypeRef<>() {
                });
        assertEquals("Required attributes 'dateOfBirth, firstName' missing for verification Geverifieerd", errorResult.get("message"));
    }

    @Test
    void updateEduIDHappyFlowWithNewUser() {
        ExternalEduID externalEduID = new ExternalEduID(
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
        ExternalEduID externalEduIDResult = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduID)
                .post("/api/remote-creation/eduid-create")
                .then()
                .statusCode(201)
                .extract()
                .as(new TypeRef<>() {
                });
        externalEduIDResult.setBrinCode("QWER");
        externalEduIDResult.setVerification(Verification.Geverifieerd);
        given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduIDResult)
                .put("/api/remote-creation/eduid-update")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(new TypeRef<>() {
                });
        String eduIDValue = externalEduIDResult.getEduIDValue();
        User user = this.findUserByEduIDValue(eduIDValue).get();

        assertEquals(1, user.getExternalLinkedAccounts().size());
        ExternalLinkedAccount externalLinkedAccount = user.getExternalLinkedAccounts().get(0);
        assertEquals(externalEduIDResult.getBrinCode(), externalLinkedAccount.getBrinCode());
        assertEquals(Verification.Geverifieerd, externalLinkedAccount.getVerification());
    }

    @Test
    void updateEduIDHappyFlowWithExistingUser() {
        ExternalEduID externalEduID = new ExternalEduID(
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
        ExternalEduID updatedExternalEduID = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduID)
                .put("/api/remote-creation/eduid-update")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(new TypeRef<>() {
                });
        String eduIDValue = updatedExternalEduID.getEduIDValue();
        User user =this.findUserByEduIDValue(eduIDValue).get();
        assertEquals(email, user.getEmail());
        assertEquals(1, user.getExternalLinkedAccounts().size());

        ExternalLinkedAccount externalLinkedAccount = user.getExternalLinkedAccounts().get(0);
        assertEquals(externalLinkedAccount.getFirstName(), externalEduID.getFirstName());
        assertEquals(externalLinkedAccount.getIdpScoping(), IdpScoping.studielink);

        //Now check if the /sp/me endpoint still works, because we have an eduID without an entityID (e.g. only institutionGUID)
        Map<String, Object> me = given()
                .when()
                .accept(ContentType.JSON)
                .get("/myconext/api/sp/me")
                .as(new TypeRef<>() {
                });
        //See src/main/resources/application.yml#external-api-configuration
        String institutionGUID = "ec9d6d75-0d11-e511-80d0-005056956c1a";
        Map<String, Object> eduIDMap = ((Map<String, Map<String, Object>>) me.get("eduIdPerServiceProvider")).get(institutionGUID);
        assertEquals(eduIDValue, eduIDMap.get("value"));
    }

    @Test
    void updateEduIDNotFound() {
        ExternalEduID externalEduID = new ExternalEduID(
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
        ExternalEduID externalEduIDResult = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduID)
                .post("/api/remote-creation/eduid-create")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(new TypeRef<>() {
                });
        externalEduIDResult.setEduIDValue(UUID.randomUUID().toString());
        externalEduIDResult.setEmail("not@existe.here");
        given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduIDResult)
                .put("/api/remote-creation/eduid-update")
                .then()
                .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void updateEduIDExternalAccountGone() {
        ExternalEduID externalEduID = new ExternalEduID(
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
        ExternalEduID externalEduIDResult = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduID)
                .post("/api/remote-creation/eduid-create")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(new TypeRef<>() {
                });
        //Now delete the externalAccount
        String eduIDValue = externalEduIDResult.getEduIDValue();
        User user = this.findUserByEduIDValue(eduIDValue).get();
        user.getExternalLinkedAccounts().clear();
        userRepository.save(user);

        given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduIDResult)
                .put("/api/remote-creation/eduid-update")
                .then()
                .statusCode(HttpStatus.GONE.value());
    }

    @Test
    void updateEduIDExternalAccountUpdateIdemPotent() {
        ExternalEduID externalEduID = new ExternalEduID(
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
        ExternalEduID externalEduIDResult = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduID)
                .post("/api/remote-creation/eduid-create")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .extract()
                .as(new TypeRef<>() {
                });
        String eduIDValue = externalEduIDResult.getEduIDValue();
        //Now remove the eduIDValue and verify no second external account is created
        externalEduIDResult.setEduIDValue(null);
        ExternalEduID updatedExternalEduIDResult =given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduIDResult)
                .put("/api/remote-creation/eduid-update")
                .as(new TypeRef<>() {
                });
        String newEduIDValue = updatedExternalEduIDResult.getEduIDValue();
        assertEquals(eduIDValue, newEduIDValue);

        User user = super.findUserByEduIDValue(newEduIDValue).get();
        assertEquals(1, user.getExternalLinkedAccounts().size());
    }

    @Test
    void findUserByEduIDValueWithNullCheck() {
        assertFalse(super.findUserByEduIDValue(null).isPresent());
    }

}