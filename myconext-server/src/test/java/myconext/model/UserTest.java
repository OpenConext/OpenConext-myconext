package myconext.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import myconext.exceptions.WeakPasswordException;
import myconext.manage.Manage;
import myconext.manage.MockManage;
import myconext.security.ServicesConfiguration;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

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
    public void computeEduIdForServiceProviderLastLoginDate() throws InterruptedException {
        User user = user("http://mock-sp");
        assertEquals(1, user.getEduIDS().size());
        List<ServiceProvider> serviceProviders = user.getEduIDS().getFirst().getServices();
        assertEquals(1, serviceProviders.size());
        Date lastLogin = serviceProviders.getFirst().getLastLogin();
        assertNotNull(lastLogin);
        Thread.sleep(2);
        user.computeEduIdForServiceProviderIfAbsent("http://mock-sp", manage);

        serviceProviders = user.getEduIDS().getFirst().getServices();
        assertEquals(1, serviceProviders.size());
        Date newLastLogin = serviceProviders.getFirst().getLastLogin();
        assertTrue(newLastLogin.after(lastLogin));
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
        Manage mockManage = mock(Manage.class);
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

        Map<String, EduID> eduIDMap = user.convertEduIdPerServiceProvider(new ServicesConfiguration());

        assertEquals(2, eduIDMap.size());
        List.of(entityId, otherEntityId).forEach(entityIdValue -> {
            EduID eduID = eduIDMap.get(entityIdValue);
            assertEquals(1, eduID.getServices().size());
            assertEquals(entityIdValue, eduID.getServices().get(0).getEntityId());
        });
        //The mobile version of the UserResponse is backward compatible with a single eduID value and ServiceProvider
        user.setMobileAuthentication(true);
        Map<String, EduID> eduIDMapMobile = user.convertEduIdPerServiceProvider(new ServicesConfiguration());

        assertEquals(2, eduIDMapMobile.size());
        List.of(entityId, otherEntityId).forEach(entityIdValue -> {
            EduID eduID = eduIDMapMobile.get(entityIdValue);
            assertEquals(entityIdValue, eduID.getServiceProviderEntityId());
            assertEquals(entityIdValue.concat("Name"), eduID.getServiceName());
            assertEquals(entityIdValue.concat("NameNl"), eduID.getServiceNameNl());
        });
    }

    @Test
    public void reconcileLinkedAccounts() {
        User user = user("sp_entity_id");
        LinkedAccount linkedAccount = new LinkedAccount();
        linkedAccount.setCreatedAt(Date.from(Instant.ofEpochMilli(423439200000L)));
        assertFalse(linkedAccount.isPreferred());

        LinkedAccount otherLinkedAccount = new LinkedAccount();
        otherLinkedAccount.setCreatedAt(Date.from(Instant.ofEpochMilli(1307052000000L)));
        ReflectionTestUtils.setField(otherLinkedAccount, "givenName", "Pol");
        ReflectionTestUtils.setField(otherLinkedAccount, "familyName", "Kropto");
        assertFalse(otherLinkedAccount.isPreferred());

        user.getLinkedAccounts().add(linkedAccount);
        user.getLinkedAccounts().add(otherLinkedAccount);

        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount();
        externalLinkedAccount.setCreatedAt(Date.from(Instant.ofEpochMilli(807052000000L)));
        assertFalse(externalLinkedAccount.isPreferred());
        user.getExternalLinkedAccounts().add(externalLinkedAccount);

        assertTrue(user.reconcileLinkedAccounts());
        assertTrue(otherLinkedAccount.isPreferred());
        assertEquals("John", user.getGivenName());
        assertEquals("Doe", user.getFamilyName());
        //Idempotency
        assertFalse(user.reconcileLinkedAccounts());
    }

    @Test
    public void hideServicesInEduID() {
        User user = new User();
        Manage mockManage = mock(Manage.class);
        String entityId = "https://sp_one";
        ServiceProvider serviceProvider = new ServiceProvider(new RemoteProvider(
                entityId, entityId.concat("Name"), entityId.concat("NameNl"), null, "logoURL"), "homeURL");
        Mockito.when(mockManage.findServiceProviderByEntityId(entityId))
                .thenReturn(Optional.of(serviceProvider));

        user.computeEduIdForServiceProviderIfAbsent(entityId, mockManage);
        Map<String, EduID> eduIdPerServiceProvider = user.convertEduIdPerServiceProvider(new ServicesConfiguration());
        assertEquals(1, eduIdPerServiceProvider.size());

        Map<String, EduID> eduIdPerServiceProviderFiltered = user.convertEduIdPerServiceProvider(new ServicesConfiguration(List.of(entityId)));
        assertEquals(0, eduIdPerServiceProviderFiltered.size());
    }

    @SneakyThrows
    @Test
    public void nudgeToAppActiveUser() {
        int nudgeAppDays = 4;
        int nudgeAppDelayDays = 7;
        long nowMillis = System.currentTimeMillis();
        User user = user("SP");
        //User has logged in within 24 hours of creation, do not nudge to app
        assertFalse(user.nudgeToApp(nudgeAppDays, nudgeAppDelayDays));
        assertEquals(0L, ReflectionTestUtils.getField(user, "lastSeenAppNudge"));

        //User has logged in after 24 hours of creation and before nudgeAppDays
        ReflectionTestUtils.setField(user, "created", Instant.now().minus(3, ChronoUnit.DAYS).toEpochMilli() / 1000);
        assertTrue(user.nudgeToApp(nudgeAppDays, nudgeAppDelayDays));
        //lastSeenAppNudge is set to now
        long lastSeenAppNudge = (long) ReflectionTestUtils.getField(user, "lastSeenAppNudge");
        assertTrue(lastSeenAppNudge >= nowMillis);

        //User was nudged to app, on login again user is not nudged again
        assertFalse(user.nudgeToApp(nudgeAppDays, nudgeAppDelayDays));
        long lastSeenAppNudgeIdempotent = (long) ReflectionTestUtils.getField(user, "lastSeenAppNudge");
        assertEquals(lastSeenAppNudge, lastSeenAppNudgeIdempotent);

        //User logs in again after long period of inactivity and is nudged again to app
        ReflectionTestUtils.setField(user, "lastSeenAppNudge", Instant.now().minus(10, ChronoUnit.DAYS).toEpochMilli() / 1000);
        //Need to sleep despite the new lastSeenAppNudge, can occur in same millisecond
        Thread.sleep(3);
        assertTrue(user.nudgeToApp(nudgeAppDays, nudgeAppDelayDays));
        long newAppNudgeIdempotent = (long) ReflectionTestUtils.getField(user, "lastSeenAppNudge");
        assertTrue(newAppNudgeIdempotent > lastSeenAppNudge);

        assertFalse(user.nudgeToApp(nudgeAppDays, nudgeAppDelayDays));
    }

    @Test
    public void nudgeToAppLongInactivity() {
        int nudgeAppDays = 4;
        int nudgeAppDelayDays = 7;
        long nowMillis = System.currentTimeMillis();
        User user = user("SP");

        //User has logged after nudge to app
        long sixDaysAgoMillis = Instant.now().minus(6, ChronoUnit.DAYS).toEpochMilli() / 1000;
        ReflectionTestUtils.setField(user, "created", sixDaysAgoMillis);
        ReflectionTestUtils.setField(user, "lastLogin", sixDaysAgoMillis);
        assertFalse(user.nudgeToApp(nudgeAppDays, nudgeAppDelayDays));
        assertEquals(0L, ReflectionTestUtils.getField(user, "lastSeenAppNudge"));

        //User logs in again very shortly after the second login
        user.computeEduIdForServiceProviderIfAbsent("SP", mock(Manage.class));
        assertTrue(user.nudgeToApp(nudgeAppDays, nudgeAppDelayDays));
        assertTrue((long) ReflectionTestUtils.getField(user, "lastSeenAppNudge") >= nowMillis);

        //But the user is not spammed with nudges after login now
        assertFalse(user.nudgeToApp(nudgeAppDays, nudgeAppDelayDays));
    }

    private User user(String serviceProviderEntityId) {
        return new User("uid", "email", "John", "John", "Doe", "schac", "en",
                serviceProviderEntityId, manage);
    }

}