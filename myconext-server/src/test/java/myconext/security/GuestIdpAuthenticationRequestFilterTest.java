package myconext.security;

import myconext.manage.MockServiceProviderResolver;
import myconext.model.LinkedAccount;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.junit.Test;
import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import saml.model.SAMLAttribute;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static myconext.AbstractIntegrationTest.user;
import static myconext.model.LinkedAccountTest.linkedAccount;
import static org.junit.Assert.*;

public class GuestIdpAuthenticationRequestFilterTest {

    private final GuestIdpAuthenticationRequestFilter subject =
            new GuestIdpAuthenticationRequestFilter();

    @Before
    public void beforeEach() {
        subject.setServiceProviderResolver(new MockServiceProviderResolver());
        subject.setUserRepository(Mockito.mock(UserRepository.class));
    }

    @Test
    public void isUserVerifiedByInstitutionNoLinkedAccounts() {
        User user = user("s@s.com", "nl");
        boolean userVerifiedByInstitution = subject.isUserVerifiedByInstitution(user, null);
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionTrue() {
        User user = user("s@s.com", "nl");
        Date createdAt = Date.from(new Date().toInstant().plus(40, ChronoUnit.DAYS));
        LinkedAccount linkedAccount = linkedAccount(createdAt, Arrays.asList("student@mobi.com"));
        user.getLinkedAccounts().add(linkedAccount);
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.LINKED_INSTITUTION);
        assertTrue(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionExpired() {
        User user = user("s@s.com", "nl");
        LinkedAccount linkedAccount = linkedAccount(Date.from(Instant.now().plus(10, ChronoUnit.DAYS)), Arrays.asList("affiliation"));
        user.getLinkedAccounts().add(linkedAccount);
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.VALIDATE_NAMES);
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionNoStudent() {
        User user = user("s@s.com", "nl");
        LinkedAccount linkedAccount = linkedAccount(new Date(), Arrays.asList("affiliation"));
        user.getLinkedAccounts().add(linkedAccount);
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.AFFILIATION_STUDENT);
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionStudent() {
        User user = user("s@s.com", "nl");
        Date createdAt = Date.from(new Date().toInstant().plus(40, ChronoUnit.DAYS));
        LinkedAccount linkedAccount = linkedAccount(createdAt, Arrays.asList("student@example.com"));
        user.getLinkedAccounts().add(linkedAccount);
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.AFFILIATION_STUDENT);
        assertTrue(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionValidNames() {
        User user = user("s@s.com", "nl");
        user.getLinkedAccounts().add(linkedAccount("John", "Doe", new Date()));
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.VALIDATE_NAMES);
        assertTrue(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionNoValidNames() {
        User user = user("s@s.com", "nl");
        user.getLinkedAccounts().add(linkedAccount("", "", new Date()));
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.VALIDATE_NAMES);
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionNoValidNamesAndExpired() {
        User user = user("s@s.com", "nl");
        Date createdAt = Date.from(Instant.now().minus(250, ChronoUnit.DAYS));
        LinkedAccount linkedAccount = linkedAccount(createdAt, Arrays.asList("affiliation"));
        user.getLinkedAccounts().add(linkedAccount);
        boolean userVerifiedByInstitution = subject.isUserVerifiedByInstitution(user, Collections.emptyList());
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void attributes() {
        User user = new User();
        user.setGivenName("Mary");
        user.setChosenName("Marrrry");
        user.setFamilyName("Poppins");
        user.setDateOfBirth(new Date());
        List<LinkedAccount> linkedAccounts = Arrays.asList(
                linkedAccount("John", "Doe", createdAt(15)),
                linkedAccount("Mary", "Poppins", createdAt(10)),
                linkedAccount("Mark", "Lee", createdAt(25))
        );
        user.setLinkedAccounts(linkedAccounts);
        List<SAMLAttribute> attributes = subject.attributes(user, "requesterEntityID");
        String givenName = attributes.stream().filter(attr -> attr.getName().equals("urn:mace:dir:attribute-def:givenName")).findFirst().get().getValue();
        String familyName = attributes.stream().filter(attr -> attr.getName().equals("urn:mace:dir:attribute-def:sn")).findFirst().get().getValue();
        String displayName = attributes.stream().filter(attr -> attr.getName().equals("urn:mace:dir:attribute-def:displayName")).findFirst().get().getValue();
        String dateOfBirth = attributes.stream().filter(attr -> attr.getName().equals("urn:schac:attribute-def:schacDateOfBirth")).findFirst().get().getValue();

        assertEquals("Mary", givenName);
        assertEquals("Poppins", familyName);
        assertEquals("Marrrry Poppins", displayName);
        //We are good to go for the next 6 years
        assertTrue(dateOfBirth.startsWith("202"));
    }

    private Date createdAt(int numberOfDaysInThePast) {
        return Date.from(new Date().toInstant().minus(numberOfDaysInThePast, ChronoUnit.DAYS));
    }


    private boolean userVerifiedByInstitution(User user, String acr) {
        return subject.isUserVerifiedByInstitution(user, Collections.singletonList(acr));
    }

}