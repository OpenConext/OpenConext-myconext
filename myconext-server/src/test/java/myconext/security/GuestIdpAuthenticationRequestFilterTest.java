package myconext.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.AbstractIntegrationTest;
import myconext.manage.ServiceNameResolver;
import myconext.model.LinkedAccount;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.saml.saml2.attribute.Attribute;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static myconext.model.LinkedAccountTest.linkedAccount;
import static myconext.security.GuestIdpAuthenticationRequestFilter.isUserVerifiedByInstitution;
import static org.junit.Assert.assertEquals;
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

    @Test
    public void isUserVerifiedByInstitutionNoValidNames() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        user.getLinkedAccounts().add(linkedAccount("John", "Doe", new Date()));
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.VALIDATE_NAMES);
        assertTrue(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionValidNames() {
        User user = AbstractIntegrationTest.user("s@s.com", "nl");
        user.getLinkedAccounts().add(linkedAccount("", "", new Date()));
        boolean userVerifiedByInstitution = userVerifiedByInstitution(user, ACR.VALIDATE_NAMES);
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void attributes() {
        GuestIdpAuthenticationRequestFilter subject =
                new GuestIdpAuthenticationRequestFilter(null,
                        null,
                        null,
                        new ServiceNameResolver(new ClassPathResource("sp_names.json"), new ObjectMapper(), false),
                        null,
                        Mockito.mock(UserRepository.class),
                        90,
                        false,
                        null,
                        null);
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
        return GuestIdpAuthenticationRequestFilter.isUserVerifiedByInstitution(user, Collections.singletonList(acr));
    }


}