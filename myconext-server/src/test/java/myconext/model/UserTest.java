package myconext.model;

import myconext.exceptions.WeakPasswordException;
import myconext.manage.MockServiceProviderResolver;
import myconext.manage.ServiceProviderResolver;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class UserTest {

    private final ServiceProviderResolver serviceProviderResolver = new MockServiceProviderResolver();

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
        String serviceProviderEntityId = "brand_new";
        String eduId = user.computeEduIdForServiceProviderIfAbsent(serviceProviderEntityId, serviceProviderResolver);
        boolean matches = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$").matcher(eduId).matches();
        assertTrue(matches);

        String existingEduId = user.computeEduIdForServiceProviderIfAbsent(serviceProviderEntityId, serviceProviderResolver);
        assertEquals(eduId, existingEduId);
    }

    @Test
    public void computeEduIdForServiceProviderIfAbsentWithIdenticalInstitutionGuid() {
        User user = user("nope");

        String eduId = user.computeEduIdForServiceProviderIfAbsent("http://mock-sp", serviceProviderResolver);
        String existingEduId = user.computeEduIdForServiceProviderIfAbsent("playground_client", serviceProviderResolver);
        assertEquals(eduId, existingEduId);
    }

    @Test
    public void computeEduIdForServiceProviderIfAbsentWithNewServiceProvider() {
        User user = user("nope");

        String eduId = user.computeEduIdForServiceProviderIfAbsent("http://mock-sp", serviceProviderResolver);
        String existingEduId = user.computeEduIdForServiceProviderIfAbsent("brand_new", serviceProviderResolver);
        assertNotEquals(eduId, existingEduId);
    }

    @Test
    public void computeEduIdForServiceProviderIfAbsentWithExiistingServiceProvider() {
        User user = user("nope");

        String eduId = user.computeEduIdForServiceProviderIfAbsent("http://mock-sp", serviceProviderResolver);
        String existingEduId = user.computeEduIdForServiceProviderIfAbsent("noInstitutionalGuid", serviceProviderResolver);
        assertNotEquals(eduId, existingEduId);
    }

    @Test(expected = WeakPasswordException.class)
    public void encryptPassword() {
        User user = new User();
        user.encryptPassword(null, null);
    }

    private User user(String serviceProviderEntityId) {
        return new User("uid", "email", "John", "Doe", "schac", "en",
                serviceProviderEntityId, new MockServiceProviderResolver());
    }

}