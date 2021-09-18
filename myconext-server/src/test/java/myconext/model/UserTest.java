package myconext.model;

import myconext.exceptions.WeakPasswordException;
import myconext.manage.MockServiceProviderResolver;
import myconext.manage.ServiceProviderResolver;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserTest {

    @Test
    public void linkedAccountsSorted() {
        User user = user("http://mock-sp", "Mock SP");
        user.getLinkedAccounts().add(LinkedAccountTest.linkedAccount("John", "Doe", new Date()));
        user.getLinkedAccounts().add(LinkedAccountTest.linkedAccount("Mary", "Steward", Date.from(Instant.now().plus(1, ChronoUnit.DAYS))));

        List<LinkedAccount> linkedAccountsSorted = user.linkedAccountsSorted();
        assertTrue(linkedAccountsSorted.get(0).getExpiresAt().toInstant().isAfter(linkedAccountsSorted.get(1).getExpiresAt().toInstant()));
    }

    @Test
    public void computeEduIdForServiceProviderIfAbsent() {
        ServiceProviderResolver serviceProviderResolver = mock(ServiceProviderResolver.class);
        when(serviceProviderResolver.resolve(anyString())).thenReturn(Optional.empty());
        User user = user("http://mock-sp", "Mock SP");
        String eduId = user.computeEduIdForServiceProviderIfAbsent("http://test.sp", serviceProviderResolver);
        boolean matches = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$").matcher(eduId).matches();
        assertTrue(matches);

        String existingEduId = user.computeEduIdForServiceProviderIfAbsent("http://test.sp", serviceProviderResolver);
        assertEquals(eduId, existingEduId);

    }

    @Test(expected = WeakPasswordException.class)
    public void encryptPassword() {
        User user = new User();
        user.encryptPassword(null, null);
    }

    private User user(String serviceProviderEntityId, String serviceProviderName) {
        return new User("uid", "email", "John", "Doe", "schac", "en",
                serviceProviderEntityId, new MockServiceProviderResolver());
    }

}