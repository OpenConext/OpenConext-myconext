package myconext.aa;

import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class AttributeAggregatorControllerTest extends AbstractIntegrationTest {

    @Value("${attribute_aggregation.user}")
    private String userName;

    @Value("${attribute_aggregation.password}")
    private String password;

    private String eppn = "1234567890@surfguest.nl";

    @Test
    public void aggregate() {
        List<UserAttribute> userAttributes = doAggregate(userName, password, "http://mock-sp", eppn);
        assertEquals(1, userAttributes.size());
    }

    @Test
    public void aggregateUserNotFound() {
        List<UserAttribute> userAttributes = doAggregate(userName, password, "http://mock-sp", "nope");
        assertEquals(0, userAttributes.size());
    }

    @Test
    public void aggregateExistingEduID() {
        User user = userRepository.findOneUserByEmailIgnoreCase("jdoe@example.com");
        String spEntityId = "http://mock-sp";
        String eduId = user.computeEduIdForServiceProviderIfAbsent(spEntityId).get();
        userRepository.save(user);
        List<UserAttribute> userAttributes = doAggregate(userName, password, spEntityId, eppn);

        assertEquals(1, userAttributes.size());
        assertEquals(eduId, userAttributes.get(0).getValues().get(0));
    }

    @Test
    public void aggregate401() {
        given()
                .when()
                .queryParam("sp_entity_id", "n/a")
                .queryParam("eduperson_principal_name", "n/a")
                .contentType(ContentType.JSON)
                .get("/myconext/api/attribute-aggregation")
                .then()
                .statusCode(401);

    }

    @Test
    public void manipulate() {
        String spEntityId = "http://mock-sp";
        Map<String, Object> res = doManipulate(spEntityId, "1234567890");
        assertEquals("1234567890@surfguest.nl", res.get("home_organization_eppn"));
    }

    @Test
    public void manipulateNewSP() {
        doManipulate("http://new-sp", "1234567890");
    }

    @Test
    public void manipulateNotFound() {
        Map<String, Object> res = doManipulate("http://new-sp", "nope");
        assertEquals(404, res.get("status"));
    }

    private List<UserAttribute> doAggregate(String user, String password, String spEntityId, String edupersonPrincipalName) {
        return given()
                .when()
                .auth().preemptive().basic(user, password)
                .queryParam("sp_entity_id", spEntityId)
                .queryParam("eduperson_principal_name", edupersonPrincipalName)
                .contentType(ContentType.JSON)
                .get("/myconext/api/attribute-aggregation")
                .then()
                .extract().body().jsonPath().getList(".", UserAttribute.class);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> doManipulate(String spEntityId, String uid) {
        Map<String, Object> res = given()
                .when()
                .auth().preemptive().basic(userName, password)
                .queryParam("sp_entity_id", spEntityId)
                .queryParam("uid", uid)
                .contentType(ContentType.JSON)
                .get("/myconext/api/attribute-manipulation")
                .as(Map.class);
        Optional<User> optionalUser = userRepository.findUserByUid(uid);
        optionalUser.ifPresent(user -> {
            assertEquals(res.get("eduid"), user.computeEduIdForServiceProviderIfAbsent(spEntityId).get());
        });
        return res;
    }
}