package myconext.api;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.UpdateUserNameRequest;
import myconext.model.User;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

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
        UpdateUserNameRequest userNameRequest = new UpdateUserNameRequest("Mary", "Winters");
        given()
                .when()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .body(userNameRequest)
                .put("/mobile/api/sp/update")
                .then()
                .statusCode(201);
        User user = userRepository.findUserByEmail("jdoe@example.com").get();

        assertEquals(userNameRequest.getGivenName(), user.getGivenName());
        assertEquals(userNameRequest.getFamilyName(), user.getFamilyName());

    }
}