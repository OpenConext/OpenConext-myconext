package myconext.repository;

import myconext.AbstractIntegrationTest;
import myconext.model.ControlCode;
import myconext.model.User;
import myconext.security.VerificationCodeGenerator;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindUserByEmailAndRateLimitedFalse() {
        User user = userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com").get();
        assertEquals("John", user.getGivenName());

        user = userRepository.findUserByEmailAndRateLimitedFalse("JDOE@EXAMPLE.COM").get();
        assertEquals("John", user.getGivenName());

        user.setRateLimited(true);
        userRepository.save(user);

        assertTrue(userRepository.findUserByEmailAndRateLimitedFalse("jdoe@example.com").isEmpty());
    }

    @Test(expected = org.springframework.dao.DuplicateKeyException.class)
    public void testCaseInsensitiveUniqueIndex() {
        User user = new User();
        user.setEmail("JDOE@EXAMPLE.COM");
        userRepository.save(user);
    }

    @Test
    public void testTrimNames() {
        final User user = new User();
        user.setChosenName(" chosenName ");
        user.setGivenName(" givenName ");
        user.setFamilyName(" familyName ");
        final User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("chosenName", savedUser.getChosenName());
        assertEquals("givenName", savedUser.getGivenName());
        assertEquals("familyName", savedUser.getFamilyName());
        //Also check the implicit update
        savedUser.setFamilyName(" familyName ");
        final User existingUser = userRepository.save(savedUser);
        assertEquals(savedUser.getId(), existingUser.getId());
        assertEquals("familyName", existingUser.getFamilyName());
    }

    @Test
    public void testFindOneUserByEmail() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals("John", user.getGivenName());

        user = userRepository.findOneUserByEmail("JDOE@EXAMPLE.COM");
        assertEquals("John", user.getGivenName());
    }

    @Test
    public void testFindOneUserByControlCode() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        ControlCode controlCode = new ControlCode();
        controlCode.setCode(VerificationCodeGenerator.generateControlCode());
        user.setControlCode(controlCode);
        userRepository.save(user);

        user = userRepository.findByControlCode_Code(controlCode.getCode()).get();
        assertEquals(controlCode.getCode(), user.getControlCode().getCode());
    }

    @Test
    public void testFindByNewUserTrueAndCreatedLessThan() {
        User user = user("mp@example.org");
        long twoDaysAgo = (System.currentTimeMillis() / 1000L) - (2 * 24 * 60 * 60);
        ReflectionTestUtils.setField(user, "created", twoDaysAgo);
        userRepository.save(user);

        long dayAgo = new Date().toInstant().minus(1, ChronoUnit.DAYS).toEpochMilli() / 1000L;

        List<User> users = userRepository.findByNewUserTrueAndCreatedLessThan(dayAgo);
        assertEquals(1, users.size());
    }

    @Test
    public void findByLinkedAccounts_eduPersonPrincipalNameIsNotNull() {
        List<User> users = userRepository.findByLinkedAccountsIsNotEmpty();
        assertEquals(1, users.size());

        User user = userRepository.findUserByEmailAndRateLimitedFalse("mdoe@example.com").get();
        user.setLinkedAccounts(new ArrayList<>());
        userRepository.save(user);
        users = userRepository.findByLinkedAccountsIsNotEmpty();
        assertEquals(1, users.size());

        user = userRepository.findUserByEmailAndRateLimitedFalse("mdoe@example.com").get();
        user.setLinkedAccounts(null);
        userRepository.save(user);
        users = userRepository.findByLinkedAccountsIsNotEmpty();
        assertEquals(1, users.size());
    }

    @Test
    public void findUsersByEmailDomains() {
        List<String> domains = List.of("nope.com", "example.com");
        Set<String> querySet = domains.stream()
                .map(domain -> domain.replace(".", "\\."))
                .collect(Collectors.toSet());
        String regex = "@" + String.join("|", querySet) + "$";
        List<User> users = userRepository.findByEmailDomain(regex);
        assertEquals(2, users.size());
    }
}