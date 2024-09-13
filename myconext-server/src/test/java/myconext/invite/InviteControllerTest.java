package myconext.invite;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.EduID;
import myconext.model.ServiceProvider;
import myconext.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

class InviteControllerTest extends AbstractIntegrationTest {

    @Test
    void provisionEduid() {
        EduIDProvision eduIDProvision = new EduIDProvision(
                //See src/test/resources/users.json
                "3060b9ce-9cf2-4e5b-8164-bf0a2b706720",
                //See src/main/resources/manage/saml20_idp.json
                "409542a0-0d11-e511-80d0-005056956c1a"
        );
        EduIDProvision newEduIDProvision = given()
                .when()
                .auth().preemptive().basic("invite", "secret")
                .contentType(ContentType.JSON)
                .body(eduIDProvision)
                .post("/myconext/api/invite/provision-eduid")
                .as(new TypeRef<>() {
                });
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        EduID newEduID = user.getEduIDS().stream().filter(eduID -> eduID.getValue().equals(newEduIDProvision.getEduIDValue()))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        List<ServiceProvider> services = newEduID.getServices();
        assertEquals(1, services.size());
        ServiceProvider serviceProvider = services.get(0);
        assertEquals("SURFdrops Beta IdP",serviceProvider.getName());
        assertEquals(newEduIDProvision.getInstitutionGUID(), serviceProvider.getInstitutionGuid());
        assertNull(serviceProvider.getEntityId());

        //Ensure the call is idempotent
        EduIDProvision existingEduIDProvision = given()
                .when()
                .auth().preemptive().basic("invite", "secret")
                .contentType(ContentType.JSON)
                .body(newEduIDProvision)
                .post("/myconext/api/invite/provision-eduid")
                .as(new TypeRef<>() {
                });
        assertEquals(newEduIDProvision.getEduIDValue(), existingEduIDProvision.getEduIDValue());
    }

    @Test
    void provisionEduidUserNotFound() {
        given()
                .when()
                .auth().preemptive().basic("invite", "secret")
                .contentType(ContentType.JSON)
                .body(new EduIDProvision(
                        UUID.randomUUID().toString(),
                        //See src/main/resources/manage/saml20_idp.json
                        "409542a0-0d11-e511-80d0-005056956c1a"
                ))
                .post("/myconext/api/invite/provision-eduid")
                .then()
                .statusCode(404);
    }

    @Test
    void provisionEduidIdentityProviderNotFound() {
        given()
                .when()
                .auth().preemptive().basic("invite", "secret")
                .contentType(ContentType.JSON)
                .body(new EduIDProvision(
                        //See src/test/resources/users.json
                        "3060b9ce-9cf2-4e5b-8164-bf0a2b706720",
                        UUID.randomUUID().toString()
                ))
                .post("/myconext/api/invite/provision-eduid")
                .then()
                .statusCode(404);
    }
}