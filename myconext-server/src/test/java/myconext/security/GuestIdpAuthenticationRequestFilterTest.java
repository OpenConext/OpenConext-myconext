package myconext.security;

import myconext.AbstractIntegrationTest;
import myconext.geo.WhoIsGeoLocation;
import myconext.manage.MockServiceProviderResolver;
import myconext.model.LinkedAccount;
import myconext.model.User;
import myconext.repository.UserLoginRepository;
import myconext.repository.UserRepository;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.saml.saml2.attribute.Attribute;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static myconext.model.LinkedAccountTest.linkedAccount;
import static org.junit.Assert.*;

public class GuestIdpAuthenticationRequestFilterTest {

    private final int expiryNonValidatedDurationDays = 180;
    private final int removalNonValidatedDurationDays = 360;

    private final GuestIdpAuthenticationRequestFilter subject = new GuestIdpAuthenticationRequestFilter(null,
            null,
            null,
            new MockServiceProviderResolver(),
            null,
            Mockito.mock(UserRepository.class),
            Mockito.mock(UserLoginRepository.class),
            Mockito.mock(WhoIsGeoLocation.class),
            90,
            1,
            1,
            false,
            null,
            null,
            expiryNonValidatedDurationDays,
            removalNonValidatedDurationDays);

    @Test
    public void isUserVerifiedByInstitutionNoLinkedAccounts() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        boolean userVerifiedByInstitution = subject.isUserVerifiedByInstitution(user, null);
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionTrue() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        Date createdAt = Date.from(new Date().toInstant().plus(removalNonValidatedDurationDays, ChronoUnit.DAYS)) ;
        LinkedAccount linkedAccount = linkedAccount(createdAt, Arrays.asList("student@mobi.com"));
        user.getLinkedAccounts().add(linkedAccount);
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.LINKED_INSTITUTION);
        assertTrue(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionExpired() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        LinkedAccount linkedAccount = linkedAccount(Date.from(Instant.now().plus(10, ChronoUnit.DAYS)), Arrays.asList("affiliation"));
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
        Date createdAt = Date.from(new Date().toInstant().plus(removalNonValidatedDurationDays, ChronoUnit.DAYS)) ;
        LinkedAccount linkedAccount = linkedAccount(createdAt, Arrays.asList("student@example.com"));
        user.getLinkedAccounts().add(linkedAccount);
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.AFFILIATION_STUDENT);
        assertTrue(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionValidNames() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        user.getLinkedAccounts().add(linkedAccount("John", "Doe", new Date()));
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.VALIDATE_NAMES);
        assertTrue(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionNoValidNames() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        user.getLinkedAccounts().add(linkedAccount("", "", new Date()));
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.VALIDATE_NAMES);
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionNoValidNamesAndExpired() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        Date createdAt = Date.from(Instant.now().plus(10, ChronoUnit.DAYS));
        LinkedAccount linkedAccount = linkedAccount(createdAt, Arrays.asList("affiliation"));
        user.getLinkedAccounts().add(linkedAccount);
        boolean userVerifiedByInstitution = subject.isUserVerifiedByInstitution(user, Collections.emptyList());
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void attributes() {
        User user = new User();
        List<LinkedAccount> linkedAccounts = Arrays.asList(
                linkedAccount("John", "Doe", createdAt(15)),
                linkedAccount("Mary", "Poppins", createdAt(10)),
                linkedAccount("Mark", "Lee", createdAt(25))
        );
        user.setLinkedAccounts(linkedAccounts);
        List<Attribute> attributes = subject.attributes(user, "requesterEntityID", Collections.singletonList(ACR.VALIDATE_NAMES));
        String givenName = (String) attributes.stream().filter(attr -> attr.getName().equals("urn:mace:dir:attribute-def:givenName")).findFirst().get().getValues().get(0);
        String familyName = (String) attributes.stream().filter(attr -> attr.getName().equals("urn:mace:dir:attribute-def:sn")).findFirst().get().getValues().get(0);

        assertEquals("Mary", givenName);
        assertEquals("Poppins", familyName);
    }

    private Date createdAt(int numberOfDaysInThePast) {
        return Date.from(new Date().toInstant().minus(numberOfDaysInThePast, ChronoUnit.DAYS));
    }


    private boolean userVerifiedByInstitution(User user, String acr) {
        return subject.isUserVerifiedByInstitution(user, Collections.singletonList(acr));
    }


}