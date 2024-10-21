package myconext.model;

import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class LinkedAccountTest {

    @Test
    public void areNamesValidated() {
        LinkedAccount linkedAccount = linkedAccount(null, null, new Date());
        assertFalse(linkedAccount.areNamesValidated());

        linkedAccount = linkedAccount("John", "Doe", new Date());
        assertTrue(linkedAccount.areNamesValidated());
    }

    @Test
    public void schacHomeCaseInsensitive() {
        LinkedAccount linkedAccount = new LinkedAccount("id", "SCHAC", "eppn", "subjectId",
                "givenName", "familyName", Arrays.asList("student"), false, new Date(), new Date());
        assertEquals("schac", linkedAccount.getSchacHomeOrganization());
    }

    public static LinkedAccount linkedAccount(String givenName, String familyName, Date createdAt) {
        return new LinkedAccount("id", "schac", "eppn", "subjectId",
                givenName, familyName, Arrays.asList("student"), false, createdAt, expired(createdAt));
    }

    public static LinkedAccount linkedAccount(Date createdAt, List<String> affiliations) {
        return new LinkedAccount("id", "schac", "eppn", "subjectId",
                null, null, affiliations, false, createdAt, expired(createdAt));
    }

    private static Date expired(Date createdAt) {
        return Date.from(createdAt.toInstant().plus(90, ChronoUnit.DAYS));
    }

}