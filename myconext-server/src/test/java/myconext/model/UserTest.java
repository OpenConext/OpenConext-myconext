package myconext.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.exceptions.WeakPasswordException;
import myconext.manage.Manage;
import myconext.manage.MockManage;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

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
        String eduId = user.computeEduIdForServiceProviderIfAbsent("www.spd633wts00.nl", manage);
        String existingEduId = user.computeEduIdForServiceProviderIfAbsent("https://teams1.test3.surfconext.nl/shibboleth", manage);
        assertEquals(eduId, existingEduId);
    }

    @Test(expected = WeakPasswordException.class)
    public void encryptPassword() {
        User user = new User();
        user.encryptPassword(null, null);
    }

    private User user(String serviceProviderEntityId) {
        return new User("uid", "email", "John", "John", "Doe", "schac", "en",
                serviceProviderEntityId, manage);
    }

}