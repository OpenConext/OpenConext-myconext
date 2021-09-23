package myconext.aa;

import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.manage.MockServiceProviderResolver;
import myconext.model.LinkedAccount;
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
    private String attributeAggregationUserName;

    @Value("${attribute_aggregation.password}")
    private String attributeAggregationPassword;

    @Value("${attribute_manipulation.user}")
    private String attributeManipulationUserName;

    @Value("${attribute_manipulation.password}")
    private String attributeManipulationPassword;

    private String eppn = "1234567890@surfguest.nl";

    private String uid = "1234567890";
    private String eduid = "fc75dcc7-6def-4054-b8ba-3c3cc504dd4b";

    @Test
    public void aggregate() {
        List<UserAttribute> userAttributes = doAggregate(attributeAggregationUserName, attributeAggregationPassword, "http://mock-sp", eppn);
        assertEquals(1, userAttributes.size());
    }

    @Test
    public void aggregateUserNotFound() {
        List<UserAttribute> userAttributes = doAggregate(attributeAggregationUserName, attributeAggregationPassword, "http://mock-sp", "nope");
        assertEquals(0, userAttributes.size());
    }

    @Test
    public void aggregateExistingEduID() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        String spEntityId = "http://mock-sp";
        String eduId = user.computeEduIdForServiceProviderIfAbsent(spEntityId, new MockServiceProviderResolver());
        userRepository.save(user);
        List<UserAttribute> userAttributes = doAggregate(attributeAggregationUserName, attributeAggregationPassword, spEntityId, eppn);

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
    public void aggregate400WrongUser() {
        given()
                .when()
                .auth().preemptive().basic(attributeManipulationUserName, attributeManipulationPassword)
                .queryParam("sp_entity_id", "n/a")
                .queryParam("eduperson_principal_name", "n/a")
                .contentType(ContentType.JSON)
                .get("/myconext/api/attribute-aggregation")
                .then()
                .statusCode(400);
    }

    @Test
    public void manipulate() {
        String spEntityId = "http://mock-sp";
        User user = userRepository.findUserByUid(uid).get();
        LinkedAccount linkedAccount = user.getLinkedAccounts().get(0);
        String value = user.getEduIDS().get(0).getValue();

        Map<String, Object> res = doManipulate(spEntityId, value, uid, linkedAccount.getInstitutionIdentifier());
        assertEquals(linkedAccount.getEduPersonPrincipalName(), res.get("eduperson_principal_name"));
    }

    @Test
    public void manipulateNewSP() {
        doManipulate("http://new-sp", eduid, uid, null);
    }

    @Test
    public void manipulateNotFound() {
        Map<String, Object> res = doManipulate("http://new-sp", "nope", "nope", null);
        assertEquals(0, res.size());
    }

    @Test
    public void manipulate400WrongUser() {
        given()
                .when()
                .auth().preemptive().basic(attributeAggregationUserName, attributeAggregationPassword)
                .queryParam("sp_entity_id", "n/a")
                .queryParam("eduid", "n/a")
                .contentType(ContentType.JSON)
                .get("/myconext/api/attribute-manipulation")
                .then()
                .statusCode(400);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void eduIdDuplicates() {
        Map<String, List<Map<String, Object>>> results = given()
                .when()
                .auth().preemptive().basic(attributeAggregationUserName, attributeAggregationPassword)
                .contentType(ContentType.JSON)
                .get("/myconext/api/system/eduid-duplicates")
                .as(Map.class);
        assertEquals(1, results.size());
        assertEquals("ad93daef-0911-e511-80d0-005056956c1a", results.keySet().iterator().next());
        assertEquals(2, results.values().iterator().next().size());
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
    private Map<String, Object> doManipulate(String spEntityId, String eduid, String uid, String spInstitutionIdentifier) {
        Map<String, Object> res = given()
                .when()
                .auth().preemptive().basic(attributeManipulationUserName, attributeManipulationPassword)
                .queryParam("sp_entity_id", spEntityId)
                .queryParam("eduid", eduid)
                .queryParam("sp_institution_guid", spInstitutionIdentifier)
                .contentType(ContentType.JSON)
                .get("/myconext/api/attribute-manipulation")
                .as(Map.class);
        Optional<User> optionalUser = userRepository.findUserByUid(uid);
        optionalUser.ifPresent(user -> {
            assertEquals(res.get("eduid"),
                    user.computeEduIdForServiceProviderIfAbsent(spEntityId, new MockServiceProviderResolver()));
        });
        return res;
    }
}