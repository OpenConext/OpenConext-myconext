package myconext.api;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import myconext.AbstractIntegrationTest;
import myconext.model.*;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.*;
import static org.junit.Assert.assertNull;

public class UserMobileControllerTest extends AbstractIntegrationTest {

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8098);

    @Test
    @SuppressWarnings("unchecked")
    public void me() throws IOException {
        Map<String, Object> results = given()
                .when()
                .accept(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .get("/mobile/api/sp/me")
                .as(Map.class);
        assertEquals("jdoe@example.com", results.get("email"));
    }

    @Test
    public void updateUserProfile() throws IOException {
        UpdateUserNameRequest userNameRequest = new UpdateUserNameRequest("Annie", "Anna", "Winters");
        given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .auth().oauth2(doOpaqueAccessToken(true, new String[]{"eduid.nl/mobile"}, "introspect_no_linked_accounts"))
                .body(userNameRequest)
                .put("/mobile/api/sp/update")
                .then()
                .statusCode(201);
        User user = userRepository.findUserByEmailAndRateLimitedFalse("mdoe@example.com").get();

        assertEquals(userNameRequest.getChosenName(), user.getChosenName());
        assertEquals(userNameRequest.getGivenName(), user.getGivenName());
        assertEquals(userNameRequest.getFamilyName(), user.getFamilyName());
    }

    @Test
    public void updateUserProfileOldAPI() throws IOException {
        UpdateUserNameRequest userNameRequest = new UpdateUserNameRequest(null, "Anna", null);
        given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .auth().oauth2(doOpaqueAccessToken(true, new String[]{"eduid.nl/mobile"}, "introspect_no_linked_accounts"))
                .body(userNameRequest)
                .put("/mobile/api/sp/update")
                .then()
                .statusCode(201);
        User user = userRepository.findUserByEmailAndRateLimitedFalse("mdoe@example.com").get();

        assertEquals(userNameRequest.getGivenName(), user.getChosenName());
    }

    @Test
    public void institutionNames() throws IOException {
        IdentityProvider identityProvider = given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .queryParam("schac_home", "rocmn.nl")
                .get("/mobile/api/sp/institution/names")
                .as(IdentityProvider.class);

        assertEquals("ROC Midden Nederland", identityProvider.getName());
        assertEquals("ROC Midden Nederland", identityProvider.getNameNl());
    }

    @Test
    public void createEduID() {
        CreateAccount createAccount = new CreateAccount("kasd.doe@unit.org", "Kasd", "Doe", "mobile.api.client_id");
        given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(createAccount)
                .post("/mobile/api/idp/create")
                .then()
                .statusCode(201);
        User user = userRepository.findUserByEmailAndRateLimitedFalse(createAccount.getEmail()).get();

        assertEquals(createAccount.getRelyingPartClientId(), user.getEduIDS().get(0).getServices().get(0).getEntityId());
        assertNotNull(user.getEduPersonPrincipalName());

        given().redirects().follow(false)
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .queryParam("h", user.getCreateFromInstitutionKey())
                .get("/mobile/api/create-from-mobile-api")
                .then()
                .statusCode(302)
                .header("Location", "http://localhost:3000/client/mobile/created?new=true");
    }

    @Test
    public void createEduIDInApp() {
        CreateAccount createAccount = new CreateAccount("kasd.doe@unit.org", "Kasd", "Doe", "mobile.api.client_id");
        given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .body(createAccount)
                .queryParam("in-app", true)
                .post("/mobile/api/idp/create")
                .then()
                .statusCode(201);
        User user = userRepository.findOneUserByEmail(createAccount.getEmail());

        assertEquals(createAccount.getRelyingPartClientId(), user.getEduIDS().getFirst().getServices().getFirst().getEntityId());
        assertNotNull(user.getEduPersonPrincipalName());

        given().redirects().follow(false)
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .queryParam("h", user.getCreateFromInstitutionKey())
                .get("/mobile/api/create-from-mobile-api/in-app")
                .then()
                .statusCode(302)
                .header("Location", "http://localhost:3000/client/mobile/created?new=true");
    }

    @SneakyThrows
    @Test
    public void createUserControlCodeMobileApi() {
        clearExternalAccounts("jdoe@example.com");
        ControlCode controlCode = new ControlCode("Lee", "Harpers", "01 Mar 1977");
        ControlCode responseControlCode = given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .body(controlCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .when()
                .post("/mobile/api/sp/control-code")
                .as(ControlCode.class);
        assertEquals(5, responseControlCode.getCode().length());
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(user.getControlCode().getCode(), responseControlCode.getCode());

        Map<String, Object> userResponse = given()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .when()
                .delete("/mobile/api/sp/control-code")
                .as(new TypeRef<>() {
                });
        assertFalse(userResponse.containsKey("controlCode"));

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");
        assertNull(userFromDB.getControlCode());
    }

}