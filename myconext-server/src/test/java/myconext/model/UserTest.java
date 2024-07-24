package myconext.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.exceptions.WeakPasswordException;
import myconext.manage.Manage;
import myconext.manage.MockManage;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserTest {

    private final Manage manage = new MockManage(new ObjectMapper());

    @Test
    public void linkedAccountsSorted() {
        User user = user("http://mock-sp");
        user.getLinkedAccounts().add(LinkedAccountTest.linkedAccount("John", "Doe", new Date()));
        user.getLinkedAccounts().add(LinkedAccountTest.linkedAccount("Mary", "Steward", Date.from(Instant.now().plus(1, ChronoUnit.DAYS))));

        List<LinkedAccount> linkedAccountsSorted = user.linkedAccountsSorted();
        assertTrue(linkedAccountsSorted.get(0).getExpiresAt().toInstant().isAfter(linkedAccountsSorted.get(1).getExpiresAt().toInstant()));
    }

    @Test
    public void computeEduIdForServiceProviderIfAbsent() {
        User user = user("http://mock-sp");
        String entityId = "brand_new";
        String eduId = user.computeEduIdForServiceProviderIfAbsent(entityId, manage);
        boolean matches = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$").matcher(eduId).matches();
        assertTrue(matches);

        String existingEduId = user.computeEduIdForServiceProviderIfAbsent(entityId, manage);
        assertEquals(eduId, existingEduId);
    }

    @Test
    public void computeEduIdForServiceProviderIfAbsentWithIdenticalInstitutionGuid() {
        User user = user("nope");
        //See static providers JSON in src/main/resources/manage
        //"coin:institution_guid": "EC82B820-0A11-E511-80D0-005056956C1A",
        String eduId = user.computeEduIdForServiceProviderIfAbsent("www.spd633wts00.nl", manage);
        String existingEduId = user.computeEduIdForServiceProviderIfAbsent("https://manage.surfconext.nl/shibboleth", manage);
        assertEquals(eduId, existingEduId);
    }

    @Test(expected = WeakPasswordException.class)
    public void encryptPassword() {
        User user = new User();
        user.encryptPassword(null, null);
    }


    /*
     * Bugfix confirmation for https://www.pivotaltracker.com/story/show/187906562, "Backwards compatible eduIdPerServiceProvider for apps"
     *
     * When an eduID is computed for a new SP, but with an institution_guid that is equal to the institution_guid of an existing eduID, then no
     * new service is made, but the existing one is overridden. This is introduced in the remoteAPI where we compute eduID values for IdP's.
     *
     * Solution is to only match on institutionGuid when the entityId is null (which is only the case for IdP's in the remoteCreation API)
     */
    @Test
    public void bugFixForUpdateExistingEduIDAndNotAddNewService() {
        User user = new User();
        Manage mockManage = Mockito.mock(Manage.class);
        String entityId = "https://sp_one";
        String institutionGuid = UUID.randomUUID().toString();
        ServiceProvider serviceProvider = new ServiceProvider(new RemoteProvider(
                entityId, entityId.concat("Name"), entityId.concat("NameNl"), institutionGuid, "logoURL"), "homeURL");
        Mockito.when(mockManage.findServiceProviderByEntityId(entityId))
                .thenReturn(Optional.of(serviceProvider));

        user.computeEduIdForServiceProviderIfAbsent(entityId, mockManage);
        assertEquals(1, user.getEduIDS().size());
        assertEquals(1, user.getEduIDS().get(0).getServices().size());
        //Now compute a new eduID for a different SP with the same institutionID
        String otherEntityId = "https://sp_two";
        ServiceProvider otherServiceProvider = new ServiceProvider(new RemoteProvider(
                otherEntityId, otherEntityId.concat("Name"), otherEntityId.concat("NameNl"), institutionGuid, "logoURL"), "homeURL");
        Mockito.when(mockManage.findServiceProviderByEntityId(otherEntityId))
                .thenReturn(Optional.of(otherServiceProvider));
        user.computeEduIdForServiceProviderIfAbsent(otherEntityId, mockManage);
        assertEquals(1, user.getEduIDS().size());
        assertEquals(2, user.getEduIDS().get(0).getServices().size());

        Map<String, EduID> eduIDMap = user.convertEduIdPerServiceProvider();

        assertEquals(2, eduIDMap.size());
        List.of(entityId, otherEntityId).forEach(entityIdValue -> {
            EduID eduID = eduIDMap.get(entityIdValue);
            assertEquals(1, eduID.getServices().size());
            assertEquals(entityIdValue, eduID.getServices().get(0).getEntityId());
        });

        user.setMobileAuthentication(true);
        Map<String, EduID> eduIDMapMobile = user.convertEduIdPerServiceProvider();

        assertEquals(2, eduIDMapMobile.size());
        List.of(entityId, otherEntityId).forEach(entityIdValue -> {
            EduID eduID = eduIDMapMobile.get(entityIdValue);
            assertEquals(entityIdValue, eduID.getServiceProviderEntityId());
            assertEquals(entityIdValue.concat("Name"), eduID.getServiceName());
            assertEquals(entityIdValue.concat("NameNl"), eduID.getServiceNameNl());
        });
    }

    private User user(String serviceProviderEntityId) {
        return new User("uid", "email", "John", "John", "Doe", "schac", "en",
                serviceProviderEntityId, manage);
    }

}