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
import static org.junit.jupiter.api.Assertions.*;

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

        String eduIDValue = (String) result.get("eduIDValue");
        User user = userRepository.findOneUserByEmail(email);
        EduID provisionedEduID = user.getEduIDS().stream()
                .filter(eduID -> eduID.getValue().equals(eduIDValue))
                .findFirst()
                .orElseThrow();
        assertEquals(1, provisionedEduID.getServices().size());
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
        String institutionGUID = "8017e83f-bca7-e911-90f2-0050569571ea";
        EduID newEduID = user.getEduIDS().stream()
                .filter(anEduID -> anEduID.getServices().stream().anyMatch(service -> institutionGUID.equals(service.getInstitutionGuid())))
                .findFirst().get();
        assertEquals(eduID.getValue(), newEduID.getValue());
    }

    @Test
    void eduIDForInstitutionBatchHappyFlow() {
        List<EduIDAssignedValue> eduIDAssignedValues = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                //See src/test/resources/users.json eduIDs#value
                .body(new EduIDInstitutionPseudonymBatch("ST42",
                        List.of("fc75dcc7-6def-4054-b8ba-3c3cc504dd4b","fc75dcc7-6def-4054-b8ba-3c3cc504dd4b","nope")))
                .post("/api/remote-creation/eduid-institution-pseudonym-batch")
                .as(new TypeRef<>() {
                });
        assertEquals(2, eduIDAssignedValues.size());
        String value = eduIDAssignedValues.getFirst().getValue();
        User user = this.findUserByEduIDValue(value).get();
        //See src/main/resources/manage/saml20_idp.json read by MockManage
        String institutionGUID = "8017e83f-bca7-e911-90f2-0050569571ea";
        EduID newEduID = user.getEduIDS().stream()
                .filter(anEduID -> anEduID.getServices().stream().anyMatch(service -> institutionGUID.equals(service.getInstitutionGuid())))
                .findFirst().get();
        assertEquals(value, newEduID.getValue());
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
        NewExternalEduID externalEduID = new NewExternalEduID(
                "new@user.com",
                "Mary",
                "Mary",
                "von",
                "Munich",
                "19880327",
                UUID.randomUUID().toString(),
                Verification.Decentraal,
                List.of("ST42")
        );
        UpdateExternalEduID externalEduIDResult = given()
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
        assertEquals("von Munich", user.getFamilyName());

        EduID newEduID = user.getEduIDS().stream()
                .filter(anEduID -> anEduID.getValue().equals(eduIDValue))
                .findFirst().get();
        assertEquals(1, newEduID.getServices().size());
        //See src/main/resources/application.yml#external-api-configuration
        String institutionGUID = "ec9d6d75-0d11-e511-80d0-005056956c1a";
        assertEquals(institutionGUID, newEduID.getServices().get(0).getInstitutionGuid());

        assertEquals(1, user.getExternalLinkedAccounts().size());
        ExternalLinkedAccount externalLinkedAccount = user.getExternalLinkedAccounts().get(0);
        assertEquals(IdpScoping.studielink, externalLinkedAccount.getIdpScoping());
        assertEquals("student@aap.nl", externalLinkedAccount.getAffiliations().getFirst());
    }

    @Test
    void createEduIDHappyFlowNoLastNamePrefix() {
        NewExternalEduID externalEduID = new NewExternalEduID(
                "new@user.com",
                "Mary",
                "Mary",
                null,
                "Munich",
                "nope",
                UUID.randomUUID().toString(),
                Verification.Decentraal,
                null
        );
        UpdateExternalEduID externalEduIDResult = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .body(externalEduID)
                .post("/api/remote-creation/eduid-create")
                .as(new TypeRef<>() {
                });
        String eduIDValue = externalEduIDResult.getEduIDValue();
        User user = this.findUserByEduIDValue(eduIDValue).get();
        assertEquals("Munich", user.getFamilyName());
        assertNull(user.getDateOfBirth());
    }

    @Test
    void createEduIDConflict() {
        NewExternalEduID externalEduID = new NewExternalEduID(
                email,
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
        NewExternalEduID externalEduID = new NewExternalEduID(
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
        List<String> expected = Stream.of("chosenName", "identifier", "lastName", "verification").sorted().toList();
        List<Object> actual = ((List<Map<String, Object>>) errorResult.get("errors")).stream()
                .map(m -> ((Map<String, Object>) m.get("cause")).get("field")).sorted().toList();
        assertEquals(expected, actual);
    }

    @Test
    void createEduIDValidationException() {
        NewExternalEduID externalEduID = new NewExternalEduID(
                "new@eduid.com",
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
        UpdateExternalEduID externalEduID = new UpdateExternalEduID(
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
        UpdateExternalEduID externalEduIDResult = given()
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
        externalEduIDResult.setBrinCodes(List.of("ST42"));
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
        assertEquals(externalEduIDResult.getBrinCodes(), externalLinkedAccount.getBrinCodes());
        assertEquals("student@aap.nl", externalLinkedAccount.getAffiliations().getFirst());
        assertEquals(Verification.Geverifieerd, externalLinkedAccount.getVerification());
    }

    @Test
    void updateEduIDHappyFlowWithExistingUser() {
        UpdateExternalEduID externalEduID = new UpdateExternalEduID(
                email,
                "3060b9ce-9cf2-4e5b-8164-bf0a2b706720",
                "Mary",
                "Mary",
                "von",
                "Munich",
                "19880327",
                UUID.randomUUID().toString(),
                Verification.Decentraal,
                null
        );
        UpdateExternalEduID updatedExternalEduID = given()
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
        User user = this.findUserByEduIDValue(eduIDValue).get();
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
        UpdateExternalEduID externalEduID = new UpdateExternalEduID(
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
        UpdateExternalEduID externalEduIDResult = given()
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
        NewExternalEduID externalEduID = new NewExternalEduID(
                "new@user.com",
                "Mary",
                "Mary",
                "von",
                "Munich",
                "19880327",
                UUID.randomUUID().toString(),
                Verification.Decentraal,
                null
        );
        UpdateExternalEduID externalEduIDResult = given()
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
                .statusCode(HttpStatus.CREATED.value());
        User userFromDB = this.findUserByEduIDValue(eduIDValue).get();
        assertEquals(1, userFromDB.getExternalLinkedAccounts().size());

    }

    @Test
    void updateEduIDExternalAccountUpdateIdemPotent() {
        NewExternalEduID externalEduID = new NewExternalEduID(
                "new@user.com",
                "Mary",
                "Mary",
                "von",
                "Munich",
                "19880327",
                UUID.randomUUID().toString(),
                Verification.Decentraal,
                null
        );
        UpdateExternalEduID externalEduIDResult = given()
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
        //Now send the update with the same eduIDand verify no second external account is created
        UpdateExternalEduID updatedExternalEduIDResult = given()
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
    void deleteEduID() {
        User user = userRepository.findUserByEmailAndRateLimitedFalse(email).get();
        assertEquals(2, user.getEduIDS().size());
        given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .pathParam("eduid", user.getEduIDS().get(0).getValue())
                .delete("/api/remote-creation/eduid-delete/{eduid}")
                .then()
                .statusCode(204);

        User updatedUser = userRepository.findUserByEmailAndRateLimitedFalse(email).get();
        assertEquals(1, updatedUser.getEduIDS().size());
    }

    @Test
    void deleteEduIDNotFound() {
        given()
                .when()
                .auth().preemptive().basic(userName, password)
                .contentType(ContentType.JSON)
                .pathParam("eduid", "nope")
                .delete("/api/remote-creation/eduid-delete/{eduid}")
                .then()
                .statusCode(404);
    }

    @Test
    void findUserByEduIDValueWithNullCheck() {
        assertFalse(super.findUserByEduIDValue(null).isPresent());
    }

}