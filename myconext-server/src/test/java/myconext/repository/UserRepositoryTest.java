package myconext.repository;

import myconext.AbstractIntegrationTest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindUserByEmail() {
        Optional<User> user = userRepository.findUserByEmail("jdoe@example.com");
        assertEquals("John", user.get().getGivenName());

        user = userRepository.findUserByEmail("JDOE@EXAMPLE.COM");
        assertEquals("John", user.get().getGivenName());
    }

    @Test
    public void testFindOneUserByEmail() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals("John", user.getGivenName());

        user = userRepository.findOneUserByEmail("JDOE@EXAMPLE.COM");
        assertEquals("John", user.getGivenName());
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
}