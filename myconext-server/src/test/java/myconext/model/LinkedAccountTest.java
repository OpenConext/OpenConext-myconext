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

    @Test
    public void isMatch() {
        LinkedAccount linkedAccount = linkedAccount("John", "Doe", new Date());
        UpdateLinkedAccountRequest request = new UpdateLinkedAccountRequest();
        assertFalse(linkedAccount.isMatch(request));

        request = new UpdateLinkedAccountRequest("nope", "nope", false, "nope", "nope");
        assertFalse(linkedAccount.isMatch(request));

        request = new UpdateLinkedAccountRequest(null, null, false, "nope", linkedAccount.getSchacHomeOrganization());
        assertTrue(linkedAccount.isMatch(request));

        request = new UpdateLinkedAccountRequest(linkedAccount.getEduPersonPrincipalName(), null, false, "nope", null);
        assertTrue(linkedAccount.isMatch(request));

        request = new UpdateLinkedAccountRequest(null, linkedAccount.getSubjectId(), false, "nope", "nope");
        assertTrue(linkedAccount.isMatch(request));
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