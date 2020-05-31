package myconext.aa;

import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

public class AttributeAggregatorControllerTest extends AbstractIntegrationTest {

    @Value("${attribute_aggregation.user}")
    private String userName;

    @Value("${attribute_aggregation.password}")
    private String password;

    @Test
    public void aggregate() {
        List<UserAttribute> userAttributes = doAggregate(userName, password, "http://mock-sp", "1234567890@surfguest.nl");
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
        List<UserAttribute> userAttributes = doAggregate(userName, password, spEntityId, "1234567890@surfguest.nl");

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

}