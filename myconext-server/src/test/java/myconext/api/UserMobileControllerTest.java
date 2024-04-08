package myconext.api;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.CreateAccount;
import myconext.model.UpdateUserNameRequest;
import myconext.model.User;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
                .auth().oauth2(doOpaqueAccessToken(true, new String[] {"eduid.nl/mobile"}, "introspect_no_linked_accounts"))
                .body(userNameRequest)
                .put("/mobile/api/sp/update")
                .then()
                .statusCode(201);
        User user = userRepository.findUserByEmail("mdoe@example.com").get();

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
                .auth().oauth2(doOpaqueAccessToken(true, new String[] {"eduid.nl/mobile"}, "introspect_no_linked_accounts"))
                .body(userNameRequest)
                .put("/mobile/api/sp/update")
                .then()
                .statusCode(201);
        User user = userRepository.findUserByEmail("mdoe@example.com").get();

        assertEquals(userNameRequest.getGivenName(), user.getChosenName());
    }

    @Test
    public void institutionNames() throws IOException {
        Map names = given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .queryParam("schac_home", "rug.nl")
                .get("/mobile/api/sp/institution/names")
                .as(Map.class);

        assertEquals("University of Groningen", names.get("displayNameEn"));
        assertEquals("Rijksuniversiteit Groningen", names.get("displayNameNl"));
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
        User user = userRepository.findUserByEmail(createAccount.getEmail()).get();

        assertEquals(createAccount.getRelyingPartClientId(), user.getEduIDS().get(0).getServiceProviderEntityId());
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
}