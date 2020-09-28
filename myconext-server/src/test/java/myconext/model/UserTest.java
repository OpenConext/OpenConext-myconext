package myconext.model;

import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        User user = user("http://mock-sp", "Mock SP");
        String eduId = user.computeEduIdForServiceProviderIfAbsent("http://test.sp", "Mock SP", "Mock SP NL").get();
        boolean matches = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$").matcher(eduId).matches();
        assertTrue(matches);

        assertFalse(user.computeEduIdForServiceProviderIfAbsent(null, null, null).isPresent());

    }

    @Test
    public void eduIdForServiceProviderNeedsUpdate() {
        User user = user("http://mock-sp", null);

        assertTrue(user.eduIdForServiceProviderNeedsUpdate("http://rp", null, null));
        assertTrue(user.eduIdForServiceProviderNeedsUpdate(null, null, null));
        assertTrue(user.eduIdForServiceProviderNeedsUpdate("http://mock-sp", "Mock SP - EN", "Mock SP - NL"));
        assertTrue(user.eduIdForServiceProviderNeedsUpdate("http://mock-sp", "Mock SP", "Mock SP - NL"));
    }

    @Test
    public void encryptPassword() {
        User user = new User();
        user.encryptPassword(null, null);
        assertNull(user.getPassword());
    }

    private User user(String serviceProviderEntityId, String serviceProviderName) {
        return new User("uid", "email", "John", "Doe", "schac",
                "aa", serviceProviderEntityId, serviceProviderName, serviceProviderName, "en");
    }

}