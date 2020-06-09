package myconext.model;

import org.junit.Test;

import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LinkedAccountTest {

    @Test
    public void areNamesValidated() {
        LinkedAccount linkedAccount = linkedAccount(null, null, new Date());
        assertFalse(linkedAccount.areNamesValidated());

        linkedAccount = linkedAccount("John", "Doe", new Date());
        assertTrue(linkedAccount.areNamesValidated());
    }

    public static LinkedAccount linkedAccount(String givenName, String familyName, Date createdAt) {
        return new LinkedAccount("id", "schac", "eppn",
                givenName, familyName, createdAt, Date.from(createdAt.toInstant().plus(90, ChronoUnit.DAYS)));
    }
}