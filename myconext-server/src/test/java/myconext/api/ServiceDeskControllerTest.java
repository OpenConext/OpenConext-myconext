package myconext.api;

import myconext.AbstractIntegrationTest;
import myconext.invite.Invite;
import myconext.invite.MockInvite;
import myconext.model.Activity;
import myconext.model.ActivityEvent;
import myconext.model.EduID;
import myconext.model.ServiceProvider;
import myconext.model.ControlCode;
import myconext.model.ExternalLinkedAccount;
import myconext.model.ServiceDeskOverview;
import myconext.model.ServiceDeskUserDetail;
import myconext.model.ServiceDeskUserSearchResult;
import myconext.model.LinkedAccount;
import myconext.model.PublicKeyCredentials;
import myconext.model.User;
import myconext.tiqr.SURFSecureID;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "service_desk_role_auto_provisioning=True",
                "feature.service_desk_active=True",
                "host_headers.active=servicedesk.test2.eduid.nl"
        })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceDeskControllerTest
        extends AbstractIntegrationTest {

    @Autowired
    private Invite invite;

    private MockInvite mockInvite() {
        return (MockInvite) invite;
    }

    @Test
    void getUserControlCode() {
        ControlCode controlCode = given()
                .when()
                .pathParam("code", "12345")
                .get("/myconext/api/servicedesk/user/{code}")
                .as(ControlCode.class);
        assertEquals("12345", controlCode.getCode());
        assertEquals("mdoe", controlCode.getUserUid());
    }

    @Test
    void getUserControlCodeNotFound() {
        given()
                .when()
                .pathParam("code", "Nope")
                .get("/myconext/api/servicedesk/user/{code}")
                .then()
                .statusCode(404);
    }

    @Test
    void validateDate() {
        Boolean valid = given()
                .when()
                .queryParam("dayOfBirth", "16 Mar 1981")
                .get("/myconext/api/servicedesk/validate")
                .as(Boolean.class);
        assertTrue(valid);
    }

    @Test
    void validateInvalidDate() {
        Boolean valid = given()
                .when()
                .queryParam("dayOfBirth", "Nope")
                .get("/myconext/api/servicedesk/validate")
                .as(Boolean.class);
        assertFalse(valid);
    }


    @Test
    void convertUserControlCode() {
        ControlCode controlCode = given()
                .when()
                .pathParam("code", "12345")
                .get("/myconext/api/servicedesk/user/{code}")
                .as(ControlCode.class);
        controlCode.setDayOfBirth("11 Mar 1987");
        controlCode.setDocumentId("QWERTY");

        given()
                .when()
                .body(controlCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .put("/myconext/api/servicedesk/approve")
                .then()
                .statusCode(201);

        User user = userRepository.findOneUserByEmail("mdoe@example.com");
        assertEquals(1, user.getExternalLinkedAccounts().size());
        assertNull(user.getControlCode());

        ExternalLinkedAccount externalLinkedAccount = user.getExternalLinkedAccounts().getFirst();
        assertEquals(controlCode.getDocumentId(), externalLinkedAccount.getDocumentId());
        //See myconext-server/src/test/resources/external_users.json
        assertEquals("example.com", externalLinkedAccount.getSchacHomeOrganization());

        DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern("dd-MM-yyyy").toFormatter();
        ZonedDateTime dateOfBirth = externalLinkedAccount.getDateOfBirth().toInstant().atZone(ZoneId.systemDefault());
        //See myconext-server/src/test/resources/users.json
        assertEquals("11-03-1987", dateTimeFormatter.format(dateOfBirth));
    }

    @Test
    void convertUserControlCodeForbidden() {
        ControlCode controlCode = new ControlCode("firstName", "lastName", "dayOfBirth");
        controlCode.setCode("12345");
        controlCode.setUserUid("bogus");

        given()
                .when()
                .body(controlCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .put("/myconext/api/servicedesk/approve")
                .then()
                .statusCode(403);
    }

    @Test
    void convertUserControlCodeLinkedAccountsPresent() {
        ControlCode controlCode = new ControlCode("firstName", "lastName", "dayOfBirth");
        controlCode.setCode("54321");

        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        user.setControlCode(controlCode);
        userRepository.save(user);

        given()
                .when()
                .body(controlCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .put("/myconext/api/servicedesk/approve")
                .then()
                .statusCode(403);
    }

    @Test
    void logout() {
        given()
                .when()
                .get("/myconext/api/servicedesk/logout")
                .then()
                .statusCode(HttpStatus.OK.value());
    }

    @Test
    void searchUsers() {
        confirmIdentityOfMdoe();

        ServiceDeskUserSearchResult result = search(Map.of());
        assertEquals(1, result.total());
        assertEquals("mdoe@example.com", result.users().getFirst().email());
        assertEquals("mdoe", result.users().getFirst().uid());
        assertFalse(result.users().getFirst().blocked());
    }

    @Test
    void searchUsersWithRoles() {
        confirmIdentityOfMdoe();
        //The institution of the service desk member is example.com
        mockInvite().setRoles("8017e83f-bca7-e911-90f2-0050569571ea",
                Map.of("mdoe@surfguest.nl", List.of("Admins", "Readers"), "other@eduid.nl", List.of("Nope")));
        try {
            assertEquals(List.of("Admins", "Readers"), search(Map.of()).users().getFirst().roles());
        } finally {
            mockInvite().clear();
        }
        assertTrue(search(Map.of()).users().getFirst().roles().isEmpty());
    }

    @Test
    void overview() {
        seedUsers();

        ServiceDeskOverview overview = given()
                .when()
                .get("/myconext/api/servicedesk/overview")
                .as(ServiceDeskOverview.class);
        //25 seeded users are linked to the institution of the service desk member, the other 5 are not
        assertNotNull(overview.firstMonth());
        assertEquals(25, overview.baseline() + overview.added().stream().mapToLong(Long::longValue).sum());
        //The first 3 users have 3 services each
        assertEquals(3, overview.services().size());
        assertEquals(3, overview.services().getFirst().users());
    }

    @Test
    void overviewOtherInstitution() {
        ServiceDeskOverview overview = given()
                .when()
                .get("/myconext/api/servicedesk/overview")
                .as(ServiceDeskOverview.class);
        assertNull(overview.firstMonth());
        assertTrue(overview.added().isEmpty());
        assertTrue(overview.services().isEmpty());
    }

    @Test
    void searchUsersOtherInstitution() {
        //The linked accounts of the test users are all for other institutions than the one of the service desk member
        assertEquals(0, search(Map.of()).total());
    }

    @Test
    void searchUsersQuery() {
        confirmIdentityOfMdoe();

        assertEquals(1, search(Map.of("query", "MDOE")).total());
        assertEquals(1, search(Map.of("query", "mary doe")).total());
        assertEquals(0, search(Map.of("query", "nope")).total());
    }

    @Test
    void searchUsersBlocked() {
        confirmIdentityOfMdoe();
        User user = userRepository.findOneUserByEmail("mdoe@example.com");
        user.setBlocked(true);
        userRepository.save(user);

        assertEquals(1, search(Map.of("blocked", "true")).total());
        assertEquals(0, search(Map.of("blocked", "false")).total());
        assertTrue(search(Map.of()).users().getFirst().blocked());
    }

    @Test
    void searchUsersMfa() {
        confirmIdentityOfMdoe();
        User user = userRepository.findOneUserByEmail("mdoe@example.com");

        assertEquals(0, search(Map.of("mfa", "app")).total());
        assertEquals(0, search(Map.of("mfa", "passkey")).total());

        user.getSurfSecureId().put(SURFSecureID.PHONE_VERIFIED, "+31612345678");
        userRepository.save(user);

        ServiceDeskUserSearchResult result = search(Map.of("mfa", "app"));
        assertEquals(1, result.total());
        assertEquals(java.util.List.of("app", "password"), result.users().getFirst().mfaMethods());
        assertEquals(1, search(Map.of("mfa", "passkey,app")).total());
        assertEquals(0, search(Map.of("mfa", "passkey")).total());
    }

    @Test
    void searchUsersPagination() {
        confirmIdentityOfMdoe();

        ServiceDeskUserSearchResult result = search(Map.of("page", "1", "size", "1"));
        assertEquals(1, result.total());
        assertEquals(1, result.page());
        assertEquals(0, result.users().size());
    }

    /**
     * Runs last (see {@link Order}), so the seeded users stay in the database after the test run. This gives enough
     * variety to try out paging, sorting and all filters of the service desk user overview in the GUI.
     */
    @Test
    @Order(Integer.MAX_VALUE)
    void searchUsersSeedPagingAndSorting() {
        confirmIdentityOfMdoe();
        seedUsers();

        //25 seeded users + mdoe, all linked to example.com. The 5 users of other.org must never be returned
        ServiceDeskUserSearchResult first = search(Map.of("size", "10"));
        assertEquals(26, first.total());
        assertEquals(10, first.users().size());
        assertEquals(0, first.page());

        assertEquals(10, search(Map.of("size", "10", "page", "1")).users().size());
        ServiceDeskUserSearchResult last = search(Map.of("size", "10", "page", "2"));
        assertEquals(6, last.users().size());
        assertEquals(0, search(Map.of("size", "10", "page", "3")).users().size());

        //All pages together contain every user exactly once
        List<String> ids = new ArrayList<>();
        for (int page = 0; page < 3; page++) {
            search(Map.of("size", "10", "page", String.valueOf(page))).users().forEach(u -> ids.add(u.id()));
        }
        assertEquals(26, ids.stream().distinct().count());

        //Sorting by name (default) ascending and descending
        List<String> asc = search(Map.of("size", "100", "sort", "name", "direction", "asc")).users()
                .stream().map(u -> u.name()).toList();
        List<String> desc = search(Map.of("size", "100", "sort", "name", "direction", "desc")).users()
                .stream().map(u -> u.name()).toList();
        assertEquals(asc.reversed(), desc);

        //Sorting by email
        List<String> emails = search(Map.of("size", "100", "sort", "email", "direction", "asc")).users()
                .stream().map(u -> u.email()).toList();
        assertEquals(emails.stream().sorted(String.CASE_INSENSITIVE_ORDER).toList(), emails);

        //Filters
        assertEquals(5, search(Map.of("size", "100", "blocked", "true")).total());
        assertEquals(21, search(Map.of("size", "100", "blocked", "false")).total());
        assertEquals(9, search(Map.of("size", "100", "mfa", "passkey")).total());
        assertEquals(9, search(Map.of("size", "100", "mfa", "app")).total());
        //mdoe has a password too
        assertEquals(8, search(Map.of("size", "100", "mfa", "password")).total());
        //Methods are combined with OR: 9 app + 9 passkey + 8 password, with overlap
        assertEquals(17, search(Map.of("size", "100", "mfa", "password,app,passkey")).total());
        assertEquals(26, search(Map.of("size", "100", "query", "example.com")).total());
        assertEquals(3, search(Map.of("size", "100", "query", "Jansen")).total());
    }

    @Test
    void userDetail() {
        confirmIdentityOfMdoe();
        String id = search(Map.of()).users().getFirst().id();

        ServiceDeskUserDetail detail = given()
                .when()
                .pathParam("id", id)
                .get("/myconext/api/servicedesk/users/{id}")
                .as(ServiceDeskUserDetail.class);
        assertEquals(id, detail.id());
        assertEquals("mdoe", detail.uid());
        assertEquals("mdoe@example.com", detail.email());
        assertEquals("mdoe@" + userRepository.findOneUserByEmail("mdoe@example.com").getSchacHomeOrganization(),
                detail.eduPersonPrincipalName());
        assertNotNull(detail.dateOfBirth());
        assertTrue(detail.verifiedBy().contains("serviceDesk"));
        assertTrue(detail.mfaMethods().contains("password"));
        assertNotNull(detail.activities());
    }

    @Test
    void userDetailServiceLogins() {
        confirmIdentityOfMdoe();
        User user = userRepository.findOneUserByEmail("mdoe@example.com");
        user.getEduIDS().add(new EduID(UUID.randomUUID().toString(), serviceProvider("https://mock-sp", "Mock SP")));
        userRepository.save(user);

        ServiceDeskUserDetail detail = given()
                .when()
                .pathParam("id", user.getId())
                .get("/myconext/api/servicedesk/users/{id}")
                .as(ServiceDeskUserDetail.class);
        Activity login = detail.activities().stream()
                .filter(activity -> activity.getEvent() == ActivityEvent.SERVICE_FIRST_LOGIN)
                .findFirst()
                .orElseThrow();
        assertEquals("Mock SP", login.getContext().get("service"));
        assertEquals(1, detail.activities().stream()
                .filter(activity -> activity.getEvent() == ActivityEvent.SERVICE_LAST_LOGIN).count());
        //Most recent first
        for (int i = 1; i < detail.activities().size(); i++) {
            assertFalse(detail.activities().get(i).getDateTime().after(detail.activities().get(i - 1).getDateTime()));
        }
    }

    private ServiceProvider serviceProvider(String entityId, String name) {
        return serviceProvider(entityId, name, null);
    }

    private ServiceProvider serviceProvider(String entityId, String name, String nameNl) {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setEntityId(entityId);
        serviceProvider.setName(name);
        serviceProvider.setNameNl(nameNl);
        serviceProvider.setLogoUrl("https://static.surfconext.nl/media/sp/eduid.png");
        serviceProvider.setHomeUrl("https://eduid.nl/");
        serviceProvider.setInstitutionGuid("ad93daef-0911-e511-80d0-005056956c1a");
        serviceProvider.setCreatedAt(new Date(System.currentTimeMillis() - 5 * 24 * 3600 * 1000L));
        serviceProvider.setLastLogin(new Date());
        return serviceProvider;
    }

    @Test
    void blockUser() {
        confirmIdentityOfMdoe();
        String id = search(Map.of()).users().getFirst().id();
        assertFalse(search(Map.of()).users().getFirst().blocked());

        given()
                .when()
                .pathParam("id", id)
                .put("/myconext/api/servicedesk/users/{id}/block")
                .then()
                .statusCode(200);

        User blocked = userRepository.findOneUserByEmail("mdoe@example.com");
        assertTrue(blocked.isBlocked());
        assertEquals(1, search(Map.of("blocked", "true")).total());
        Activity activity = blocked.recentActivities(1).getFirst();
        assertEquals(ActivityEvent.BLOCKED_ACCOUNT, activity.getEvent());
        assertNotNull(activity.getContext().get("actor"));
        assertEquals("Mary Doe", activity.getContext().get("name"));
    }

    @Test
    void unblockUser() {
        confirmIdentityOfMdoe();
        String id = search(Map.of()).users().getFirst().id();
        given().when().pathParam("id", id).put("/myconext/api/servicedesk/users/{id}/block").then().statusCode(200);

        given()
                .when()
                .pathParam("id", id)
                .put("/myconext/api/servicedesk/users/{id}/unblock")
                .then()
                .statusCode(200);

        User unblocked = userRepository.findOneUserByEmail("mdoe@example.com");
        assertFalse(unblocked.isBlocked());
        assertEquals(0, search(Map.of("blocked", "true")).total());
        assertEquals(ActivityEvent.DEBLOCKED_ACCOUNT, unblocked.recentActivities(1).getFirst().getEvent());
        assertEquals(ActivityEvent.BLOCKED_ACCOUNT, unblocked.recentActivities(2).get(1).getEvent());
    }

    @Test
    void resetMfa() {
        confirmIdentityOfMdoe();
        User user = userRepository.findOneUserByEmail("mdoe@example.com");
        user.getSurfSecureId().put("phone-verified", true);
        userRepository.save(user);
        String id = user.getId();
        assertFalse(userRepository.findOneUserByEmail("mdoe@example.com").getSurfSecureId().isEmpty());

        given()
                .when()
                .pathParam("id", id)
                .put("/myconext/api/servicedesk/users/{id}/reset-mfa")
                .then()
                .statusCode(200);

        User reset = userRepository.findOneUserByEmail("mdoe@example.com");
        assertTrue(reset.getSurfSecureId().isEmpty());
        Activity activity = reset.recentActivities(1).getFirst();
        assertEquals(ActivityEvent.MFA_RESET, activity.getEvent());
        assertEquals("Mary Doe", activity.getContext().get("name"));
    }

    @Test
    void resetMfaOtherInstitution() {
        User user = userRepository.findOneUserByEmail("mdoe@example.com");
        user.getSurfSecureId().put("phone-verified", true);
        userRepository.save(user);
        given()
                .when()
                .pathParam("id", user.getId())
                .put("/myconext/api/servicedesk/users/{id}/reset-mfa")
                .then()
                .statusCode(404);
        assertFalse(userRepository.findOneUserByEmail("mdoe@example.com").getSurfSecureId().isEmpty());
    }

    @Test
    void unblockUserOtherInstitution() {
        String id = userRepository.findOneUserByEmail("mdoe@example.com").getId();
        given()
                .when()
                .pathParam("id", id)
                .put("/myconext/api/servicedesk/users/{id}/unblock")
                .then()
                .statusCode(404);
    }

    @Test
    void blockUserOtherInstitution() {
        String id = userRepository.findOneUserByEmail("mdoe@example.com").getId();
        given()
                .when()
                .pathParam("id", id)
                .put("/myconext/api/servicedesk/users/{id}/block")
                .then()
                .statusCode(404);
        assertFalse(userRepository.findOneUserByEmail("mdoe@example.com").isBlocked());
    }

    @Test
    void userDetailOtherInstitution() {
        //Not linked to the institution of the service desk member
        String id = userRepository.findOneUserByEmail("mdoe@example.com").getId();
        given()
                .when()
                .pathParam("id", id)
                .get("/myconext/api/servicedesk/users/{id}")
                .then()
                .statusCode(404);
    }

    /**
     * Users with eduID values (3 eduID's with one service each, so 6 login activities):
     * anna.jansen@example.com, bram.devries@example.com and cleo.bakker@example.com
     */
    private void seedUsers() {
        String[][] names = {
                {"Anna", "Jansen"}, {"Bram", "de Vries"}, {"Cleo", "Bakker"}, {"Daan", "Visser"},
                {"Eva", "Smit"}, {"Finn", "Meijer"}, {"Gina", "de Boer"}, {"Hugo", "Mulder"},
                {"Iris", "de Groot"}, {"Jan", "Jansen"}, {"Kim", "Bos"}, {"Lars", "Vos"},
                {"Mila", "Peters"}, {"Noah", "Hendriks"}, {"Olga", "van Dijk"}, {"Piet", "Dekker"},
                {"Quinn", "Jansen"}, {"Rosa", "Brouwer"}, {"Sam", "de Wit"}, {"Tess", "Dijkstra"},
                {"Uma", "Smits"}, {"Vera", "de Graaf"}, {"Wout", "van der Meer"}, {"Xena", "Kok"},
                {"Yara", "Jacobs"}, {"Zoe", "Willems"}, {"Aron", "Maas"}, {"Bo", "Prins"},
                {"Cas", "Koster"}, {"Dex", "Huisman"}};
        List<List<String>> affiliations = List.of(
                List.of("student"), List.of("employee"), List.of("student", "member"), List.of("faculty"),
                List.of("staff"), List.of("alum"), List.of(), List.of("affiliate"));
        Date now = new Date();
        Date expires = new Date(now.getTime() + 180L * 24 * 3600 * 1000);
        for (int i = 0; i < names.length; i++) {
            String given = names[i][0];
            String family = names[i][1];
            String email = given.toLowerCase() + "." + family.toLowerCase().replace(" ", "") + "@example.com";
            User user = new User("seed" + i, email, given, given, family, "example.com", "en", (String) null, null);
            //The last 5 users are linked to another institution
            String org = i >= 25 ? "other.org" : "example.com";
            user.setLinkedAccounts(new ArrayList<>(List.of(new LinkedAccount("https://idp." + org, org,
                    "seed" + i + "@" + org, "subject" + i, given, family, affiliations.get(i % affiliations.size()),
                    List.of(), true, now, expires))));
            //Variety: 5 blocked, 9 with the app, 9 with a passkey, 7 with a password (some with both)
            user.setBlocked(i % 5 == 0 && i < 25);
            if (i % 2 == 0 && i < 18) {
                user.getSurfSecureId().put(SURFSecureID.PHONE_VERIFIED, "+3161234567" + (i % 10));
            }
            if (i % 3 == 0 && i < 25) {
                user.setPublicKeyCredentials(new ArrayList<>(List.of(
                        new PublicKeyCredentials("id" + i, "credential" + i, "Passkey " + i))));
            }
            //7 users (and mdoe) have a password (users 0, 4, 8 ... 24), some of them also have an app or a passkey
            if (i % 4 == 0 && i < 25) {
                ReflectionTestUtils.setField(user, "password", "{bcrypt}seed-password-hash-" + i);
            }
            //The first 3 users have 3 eduID's, each for one service (as jdoe@example.com in users.json)
            if (i < 3) {
                for (int e = 0; e < 3; e++) {
                    user.getEduIDS().add(new EduID(UUID.randomUUID().toString(),
                            serviceProvider("https://sp" + e, "Service " + e, "Service NL " + e)));
                }
            }
            //Activities, so the user details have something to show. Every user gets at least the account creation
            user.addActivity(ActivityEvent.ACCOUNT_CREATED, Map.of("institution", "idin"));
            user.addActivity(ActivityEvent.INSTITUTION_LINKED, Map.of("institution", org));
            for (int a = 0; a < i % 8; a++) {
                user.addActivity(ActivityEvent.values()[(i + a) % 8 + 4], Map.of("name", "Seed " + a));
            }
            userRepository.save(user);
        }
    }

    private void confirmIdentityOfMdoe() {
        ControlCode controlCode = given()
                .when()
                .pathParam("code", "12345")
                .get("/myconext/api/servicedesk/user/{code}")
                .as(ControlCode.class);
        controlCode.setDayOfBirth("11 Mar 1987");
        controlCode.setDocumentId("QWERTY");
        given()
                .when()
                .body(controlCode)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .put("/myconext/api/servicedesk/approve")
                .then()
                .statusCode(201);
    }

    private ServiceDeskUserSearchResult search(Map<String, String> queryParams) {
        return given()
                .when()
                .queryParams(queryParams)
                .get("/myconext/api/servicedesk/users")
                .as(ServiceDeskUserSearchResult.class);
    }
}
