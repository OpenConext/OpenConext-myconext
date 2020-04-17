package myconext.repository;

import myconext.AbstractIntegrationTest;
import myconext.model.User;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindUserByEmail() {
        Optional<User> user = userRepository.findUserByEmailIgnoreCase("jdoe@example.com");
        assertEquals("John", user.get().getGivenName());

        user = userRepository.findUserByEmailIgnoreCase("JDOE@EXAMPLE.COM");
        assertEquals("John", user.get().getGivenName());
    }

    @Test
    public void testFindOneUserByEmail() {
        User user = userRepository.findOneUserByEmailIgnoreCase("jdoe@example.com");
        assertEquals("John", user.getGivenName());

        user = userRepository.findOneUserByEmailIgnoreCase("JDOE@EXAMPLE.COM");
        assertEquals("John", user.getGivenName());
    }
}