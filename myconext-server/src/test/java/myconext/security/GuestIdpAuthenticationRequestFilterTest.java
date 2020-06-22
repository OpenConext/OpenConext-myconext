package myconext.security;

import myconext.AbstractIntegrationTest;
import myconext.model.LinkedAccount;
import myconext.model.User;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static myconext.model.LinkedAccountTest.linkedAccount;
import static myconext.security.GuestIdpAuthenticationRequestFilter.isUserVerifiedByInstitution;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GuestIdpAuthenticationRequestFilterTest {

    @Test
    public void isUserVerifiedByInstitutionNoLinkedAccounts() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        boolean userVerifiedByInstitution = isUserVerifiedByInstitution(user, null);
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionTrue() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        LinkedAccount linkedAccount = linkedAccount(new Date(), Arrays.asList("student@mobi.com"));
        user.getLinkedAccounts().add(linkedAccount);
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.LINKED_INSTITUTION);
        assertTrue(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionExpired() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        LinkedAccount linkedAccount = linkedAccount(Date.from(Instant.now().minus(365, ChronoUnit.DAYS)), Arrays.asList("affiliation"));
        user.getLinkedAccounts().add(linkedAccount);
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.VALIDATE_NAMES);
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionNoStudent() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        LinkedAccount linkedAccount = linkedAccount(new Date(), Arrays.asList("affiliation"));
        user.getLinkedAccounts().add(linkedAccount);
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.AFFILIATION_STUDENT);
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionStudent() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        LinkedAccount linkedAccount = linkedAccount(new Date(), Arrays.asList("student"));
        user.getLinkedAccounts().add(linkedAccount);
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.AFFILIATION_STUDENT);
        assertTrue(userVerifiedByInstitution);
    }

    private boolean userVerifiedByInstitution(User user, String acr) {
        return GuestIdpAuthenticationRequestFilter.isUserVerifiedByInstitution(user, Collections.singletonList(acr));
    }


}