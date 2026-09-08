package myconext.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.EduID;
import myconext.model.RemoteProvider;
import myconext.model.ServiceMigration;
import myconext.model.ServiceProvider;
import myconext.model.User;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class SystemControllerTest extends AbstractIntegrationTest {

    private static final String EXISTING_INSTITUTION_GUID = "ad93daef-0911-e511-80d0-005056956c1a";

    @Test
    public void eduIdDuplicates() {
        Map<String, List<EduID>> eduIDValues = given()
                .when()
                .auth().preemptive().basic("internal", "secret")
                .contentType(ContentType.JSON)
                .get("/myconext/api/system/eduid-duplicates")
                .as(new TypeRef<>() {
                });
        assertEquals(0, eduIDValues.size());
    }

    @Test
    public void serviceMigrationInPlaceUpdate() {
        //The user has no other eduID under the new institutionGUID, so the existing eduID is simply corrected
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.getEduIDS().clear();
        ServiceProvider serviceProvider = new ServiceProvider(
                new RemoteProvider("playground_client", "Playground", "Playground", "wrong-guid", "https://logo"),
                "https://home");
        EduID eduID = new EduID(UUID.randomUUID().toString(), serviceProvider);
        user.getEduIDS().add(eduID);
        userRepository.save(user);

        List<Map<String, String>> results = doServiceMigration("playground_client", "correct-guid", false);

        assertTrue(results.isEmpty());

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(1, userFromDB.getEduIDS().size());
        EduID updatedEduID = userFromDB.getEduIDS().get(0);
        assertEquals(eduID.getValue(), updatedEduID.getValue());
        assertEquals("correct-guid", updatedEduID.getServiceInstutionGuid());
        assertEquals("correct-guid", updatedEduID.getServices().get(0).getInstitutionGuid());
    }

    @Test
    public void serviceMigrationMerge() {
        //The user already has another eduID under the new institutionGUID, so the service is moved there
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.getEduIDS().clear();

        ServiceProvider migratingServiceProvider = new ServiceProvider(
                new RemoteProvider("playground_client", "Playground", "Playground", "wrong-guid", "https://logo"),
                "https://home");
        EduID sourceEduID = new EduID(UUID.randomUUID().toString(), migratingServiceProvider);
        user.getEduIDS().add(sourceEduID);

        ServiceProvider otherServiceProvider = new ServiceProvider(
                new RemoteProvider("other_client", "Other", "Other", EXISTING_INSTITUTION_GUID, "https://logo"),
                "https://home");
        EduID targetEduID = new EduID(UUID.randomUUID().toString(), otherServiceProvider);
        user.getEduIDS().add(targetEduID);

        userRepository.save(user);

        List<Map<String, String>> results = doServiceMigration("playground_client", EXISTING_INSTITUTION_GUID, false);

        assertEquals(1, results.size());
        assertEquals(user.getEmail(), results.get(0).get("user"));
        assertEquals(sourceEduID.getValue(), results.get(0).get("oldEduID"));
        assertEquals(targetEduID.getValue(), results.get(0).get("newEduID"));

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(1, userFromDB.getEduIDS().size());
        EduID mergedEduID = userFromDB.getEduIDS().get(0);
        assertEquals(targetEduID.getValue(), mergedEduID.getValue());
        assertEquals(2, mergedEduID.getServices().size());
        assertTrue(mergedEduID.getServices().stream().anyMatch(sp -> "playground_client".equals(sp.getEntityId())));
    }

    @Test
    public void serviceMigrationInPlaceUpdateDryRun() {
        //Rule-1 branch: dryRun must skip persistence, and still not add anything to results
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.getEduIDS().clear();
        ServiceProvider serviceProvider = new ServiceProvider(
                new RemoteProvider("playground_client", "Playground", "Playground", "wrong-guid", "https://logo"),
                "https://home");
        EduID eduID = new EduID(UUID.randomUUID().toString(), serviceProvider);
        user.getEduIDS().add(eduID);
        userRepository.save(user);

        List<Map<String, String>> results = doServiceMigration("playground_client", "correct-guid", true);

        assertTrue(results.isEmpty());

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals("wrong-guid", userFromDB.getEduIDS().get(0).getServiceInstutionGuid());
    }

    @Test
    public void serviceMigrationMergeDryRun() {
        //Rule-2 branch under dryRun: results only report the current eduID (no eduID is actually computed/created), and nothing is persisted
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.getEduIDS().clear();

        ServiceProvider migratingServiceProvider = new ServiceProvider(
                new RemoteProvider("playground_client", "Playground", "Playground", "wrong-guid", "https://logo"),
                "https://home");
        EduID sourceEduID = new EduID(UUID.randomUUID().toString(), migratingServiceProvider);
        user.getEduIDS().add(sourceEduID);

        ServiceProvider otherServiceProvider = new ServiceProvider(
                new RemoteProvider("other_client", "Other", "Other", EXISTING_INSTITUTION_GUID, "https://logo"),
                "https://home");
        EduID targetEduID = new EduID(UUID.randomUUID().toString(), otherServiceProvider);
        user.getEduIDS().add(targetEduID);

        userRepository.save(user);

        List<Map<String, String>> results = doServiceMigration("playground_client", EXISTING_INSTITUTION_GUID, true);

        assertEquals(1, results.size());
        assertEquals(user.getEmail(), results.get(0).get("user"));
        assertEquals(sourceEduID.getValue(), results.get(0).get("currentEduID"));
        assertFalse(results.get(0).containsKey("oldEduID"));
        assertFalse(results.get(0).containsKey("newEduID"));

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(2, userFromDB.getEduIDS().size());
        assertTrue(userFromDB.getEduIDS().stream().anyMatch(e -> e.getValue().equals(sourceEduID.getValue())));
        assertTrue(userFromDB.getEduIDS().stream().anyMatch(e -> e.getValue().equals(targetEduID.getValue())));
    }

    @Test
    public void serviceMigrationSplitCreatesNewEduID() {
        //sourceEduID has 3 services sharing the same (wrong) institutionGuid; only "playground_client" is migrated and there
        //is no other eduID yet for the target institutionGUID, so a brand new eduID must be created for it
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.getEduIDS().clear();
        EduID sourceEduID = threeServiceEduID();
        user.getEduIDS().add(sourceEduID);
        userRepository.save(user);

        List<Map<String, String>> results = doServiceMigration("playground_client", "correct-guid", false);

        assertEquals(1, results.size());
        assertEquals(sourceEduID.getValue(), results.get(0).get("oldEduID"));
        String newValue = results.get(0).get("newEduID");
        assertNotEquals(sourceEduID.getValue(), newValue);

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(2, userFromDB.getEduIDS().size());

        EduID remaining = userFromDB.getEduIDS().stream()
                .filter(e -> e.getValue().equals(sourceEduID.getValue())).findFirst().orElseThrow();
        assertEquals("wrong-guid", remaining.getServiceInstutionGuid());
        assertEquals(2, remaining.getServices().size());
        assertTrue(remaining.getServices().stream().anyMatch(sp -> "second".equals(sp.getEntityId())));
        assertTrue(remaining.getServices().stream().anyMatch(sp -> "third".equals(sp.getEntityId())));
        assertFalse(remaining.getServices().stream().anyMatch(sp -> "playground_client".equals(sp.getEntityId())));

        EduID created = userFromDB.getEduIDS().stream()
                .filter(e -> e.getValue().equals(newValue)).findFirst().orElseThrow();
        assertEquals("correct-guid", created.getServiceInstutionGuid());
        assertEquals(1, created.getServices().size());
        assertEquals("playground_client", created.getServices().get(0).getEntityId());
    }

    @Test
    public void serviceMigrationSplitMergesIntoExistingEduID() {
        //Same 3-service sourceEduID, but the user already has another eduID for the target institutionGUID,
        //so the migrated service must merge into that existing eduID, not create a new one
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.getEduIDS().clear();
        EduID sourceEduID = threeServiceEduID();
        user.getEduIDS().add(sourceEduID);

        ServiceProvider otherServiceProvider = new ServiceProvider(
                new RemoteProvider("other_client", "Other", "Other", EXISTING_INSTITUTION_GUID, "https://logo"),
                "https://home");
        EduID targetEduID = new EduID(UUID.randomUUID().toString(), otherServiceProvider);
        user.getEduIDS().add(targetEduID);

        userRepository.save(user);

        List<Map<String, String>> results = doServiceMigration("playground_client", EXISTING_INSTITUTION_GUID, false);

        assertEquals(1, results.size());
        assertEquals(sourceEduID.getValue(), results.get(0).get("oldEduID"));
        assertEquals(targetEduID.getValue(), results.get(0).get("newEduID"));

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(2, userFromDB.getEduIDS().size());

        EduID remaining = userFromDB.getEduIDS().stream()
                .filter(e -> e.getValue().equals(sourceEduID.getValue())).findFirst().orElseThrow();
        assertEquals(2, remaining.getServices().size());
        assertFalse(remaining.getServices().stream().anyMatch(sp -> "playground_client".equals(sp.getEntityId())));

        EduID merged = userFromDB.getEduIDS().stream()
                .filter(e -> e.getValue().equals(targetEduID.getValue())).findFirst().orElseThrow();
        assertEquals(2, merged.getServices().size());
        assertTrue(merged.getServices().stream().anyMatch(sp -> "playground_client".equals(sp.getEntityId())));
    }

    @Test
    public void serviceMigrationSplitDryRun() {
        //Split under dryRun: results only report the current eduID, and nothing is persisted - the 3-service
        //eduID must remain completely untouched
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.getEduIDS().clear();
        EduID sourceEduID = threeServiceEduID();
        user.getEduIDS().add(sourceEduID);
        userRepository.save(user);

        List<Map<String, String>> results = doServiceMigration("playground_client", "correct-guid", true);

        assertEquals(1, results.size());
        assertEquals(sourceEduID.getValue(), results.get(0).get("currentEduID"));
        assertFalse(results.get(0).containsKey("oldEduID"));
        assertFalse(results.get(0).containsKey("newEduID"));

        User userFromDB = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals(1, userFromDB.getEduIDS().size());
        EduID unchanged = userFromDB.getEduIDS().get(0);
        assertEquals(3, unchanged.getServices().size());
        assertEquals("wrong-guid", unchanged.getServiceInstutionGuid());
    }

    @Test
    public void serviceMigrationNotFound() {
        given()
                .when()
                .auth().preemptive().basic("internal", "secret")
                .contentType(ContentType.JSON)
                .body(new ServiceMigration("http://mock-sp", "correct-guid", false))
                .post("/myconext/api/system/service-migration")
                .then()
                .statusCode(404);
    }

    private EduID threeServiceEduID() {
        //"playground_client" must be a real entityId known to Manage (SystemController looks it up there); "second"/"third" need not be
        ServiceProvider first = new ServiceProvider(
                new RemoteProvider("playground_client", "First", "First", "wrong-guid", "https://logo"), "https://home");
        EduID eduID = new EduID(UUID.randomUUID().toString(), first);
        eduID.updateServiceProvider(new ServiceProvider(
                new RemoteProvider("second", "Second", "Second", "wrong-guid", "https://logo"), "https://home"));
        eduID.updateServiceProvider(new ServiceProvider(
                new RemoteProvider("third", "Third", "Third", "wrong-guid", "https://logo"), "https://home"));
        return eduID;
    }

    private List<Map<String, String>> doServiceMigration(String entityID, String institutionGUID, boolean dryRun) {
        return given()
                .when()
                .auth().preemptive().basic("internal", "secret")
                .contentType(ContentType.JSON)
                .body(new ServiceMigration(entityID, institutionGUID, dryRun))
                .post("/myconext/api/system/service-migration")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }
}