package myconext.model;

import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class UserTest {

    @Test
    public void linkedAccountsSorted() {
        User user = user();
        user.getLinkedAccounts().add(LinkedAccountTest.linkedAccount("John", "Doe", new Date()));
        user.getLinkedAccounts().add(LinkedAccountTest.linkedAccount("Mary", "Steward", Date.from(Instant.now().plus(1, ChronoUnit.DAYS))));

        List<LinkedAccount> linkedAccountsSorted = user.linkedAccountsSorted();
        assertTrue(linkedAccountsSorted.get(0).getExpiresAt().toInstant().isAfter(linkedAccountsSorted.get(1).getExpiresAt().toInstant()));
    }

    private User user() {
        return new User("uid", "email", "John", "Doe","schac","aa","http://mock-sp","en");
    }
}