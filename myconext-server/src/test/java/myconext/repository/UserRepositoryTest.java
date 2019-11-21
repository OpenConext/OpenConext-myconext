package myconext.repository;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import myconext.AbstractIntegrationTest;
import myconext.model.User;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class UserRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testFindUserByEmail() {
        Optional<User> user = userRepository.findUserByEmail("jdoe@example.com");
        assertEquals("John", user.get().getGivenName());
    }

    @Test
    public void testFindOneUserByEmail() {
        User user = userRepository.findOneUserByEmail("jdoe@example.com");
        assertEquals("John", user.getGivenName());
    }
}