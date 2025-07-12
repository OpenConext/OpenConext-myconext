package myconext.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.manage.MockManage;
import myconext.model.*;
import myconext.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void beforeEach() {
        subject.setManage(new MockManage(objectMapper));
        subject.setUserRepository(Mockito.mock(UserRepository.class));
    }

    @Test
    public void isUserVerifiedByInstitutionNoLinkedAccounts() {
        User user = user("s@s.com", "nl");
        boolean userVerifiedByInstitution = subject.isUserVerifiedByInstitution(user, null);
        assertFalse(userVerifiedByInstitution);
    }

    @Test
    public void isUserVerifiedByInstitutionNoLinkedAccountsNotVerified() {
        User user = user("s@s.com", "nl");
        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount("subjct-id", IdpScoping.studielink, true);
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        assertTrue(subject.isUserVerifiedByInstitution(user, List.of(ACR.VALIDATE_NAMES_EXTERNAL)));

        externalLinkedAccount.setVerification(Verification.Ongeverifieerd);
        assertFalse(subject.isUserVerifiedByInstitution(user, List.of(ACR.VALIDATE_NAMES_EXTERNAL)));
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
        String givenName = getValue(attributes, "urn:mace:dir:attribute-def:givenName");
        String familyName = getValue(attributes, "urn:mace:dir:attribute-def:sn");
        String displayName = getValue(attributes, "urn:mace:dir:attribute-def:displayName");
        boolean dateOfBirthPresent = attributes.stream().filter(attr -> attr.getName().equals("urn:schac:attribute-def:schacDateOfBirth")).findFirst().isPresent();

        assertEquals("Mary", givenName);
        assertEquals("Poppins", familyName);
        assertEquals("Marrrry Poppins", displayName);
        assertFalse(dateOfBirthPresent);
    }

    @Test
    public void attributesExternalLinkedAccount() {
        User user = new User();
        user.setChosenName("Chosen");
        ExternalLinkedAccount externalLinkedAccount = externalLinkedAccount(IdpScoping.studielink, Verification.Geverifieerd);
        externalLinkedAccount.setPreferred(true);
        user.getExternalLinkedAccounts().add(externalLinkedAccount);
        List<SAMLAttribute> attributes = subject.attributes(user, "requesterEntityID");
        String givenName = getValue(attributes, "urn:mace:dir:attribute-def:givenName");
        String familyName = getValue(attributes, "urn:mace:dir:attribute-def:sn");
        String displayName = getValue(attributes, "urn:mace:dir:attribute-def:displayName");
        String dateOfBirth = getValue(attributes, "urn:schac:attribute-def:schacDateOfBirth");

        assertEquals("John", givenName);
        assertEquals("Doe", familyName);
        assertEquals("Chosen Doe", displayName);
        //We are good to go for the next 6 years
        assertTrue(dateOfBirth.startsWith("202"));
    }

    @Test
    public void assurances() {
        User user = new User();
        List<SAMLAttribute> samlAttributes = subject.attributes(user, "requester");
        assertEquals(6, getEduPersonAssurancesCount(samlAttributes));

        LinkedAccount linkedAccount = new LinkedAccount();
        user.getLinkedAccounts().add(linkedAccount);
        samlAttributes = subject.attributes(user, "requester");
        assertEquals(8, getEduPersonAssurancesCount(samlAttributes));

        linkedAccount.setEduPersonAssurances(List.of("https://refeds.org/assurance/IAP/high"));
        samlAttributes = subject.attributes(user, "requester");
        assertEquals(9, getEduPersonAssurancesCount(samlAttributes));

        user.getExternalLinkedAccounts().addAll(List.of(
                externalLinkedAccount(IdpScoping.idin, null),
                externalLinkedAccount(IdpScoping.serviceDesk, null),
                externalLinkedAccount(IdpScoping.eherkenning, null),
                externalLinkedAccount(IdpScoping.studielink, Verification.Geverifieerd)
        ));

        samlAttributes = subject.attributes(user, "requester");
        assertEquals(13, getEduPersonAssurancesCount(samlAttributes));
    }

    private static long getEduPersonAssurancesCount(List<SAMLAttribute> samlAttributes) {
        return samlAttributes.stream().filter(attr -> attr.getName().equals("urn:mace:dir:attribute-def:eduPersonAssurance")).count();
    }

    private ExternalLinkedAccount externalLinkedAccount(IdpScoping idpScoping, Verification verification) {
        return new ExternalLinkedAccount(
                UUID.randomUUID().toString(),
                idpScoping,
                new VerifyIssuer(idpScoping.name(), idpScoping.name(), null),
                verification,
                UUID.randomUUID().toString(),
                idpScoping.name(),
                idpScoping.name(),
                null,
                null,
                "Johny",
                "John",
                "Doe",
                "Doe",
                null,
                null,
                null,
                null,
                new Date(),
                new Date(),
                Date.from(Instant.now().plus(365 * 5, ChronoUnit.DAYS)),
                true
        );
    }

    private String getValue(List<SAMLAttribute> attributes, String anObject) {
        return attributes.stream().filter(attr -> attr.getName().equals(anObject)).findFirst().get().getValue();
    }

    private Date createdAt(int numberOfDaysInThePast) {
        return Date.from(new Date().toInstant().minus(numberOfDaysInThePast, ChronoUnit.DAYS));
    }


    private boolean userVerifiedByInstitution(User user, String acr) {
        return subject.isUserVerifiedByInstitution(user, Collections.singletonList(acr));
    }

}