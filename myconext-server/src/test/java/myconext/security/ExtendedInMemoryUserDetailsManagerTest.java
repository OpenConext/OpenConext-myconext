package myconext.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ExtendedInMemoryUserDetailsManagerTest {

    @Test
    void illegalPassword() {
        assertThrows(IllegalArgumentException.class, () -> extendedInMemoryUserDetailManager("{noop}password"));
    }

    @Test
    void loadUserByUsername() {
        ExtendedInMemoryUserDetailsManager userDetailsManager = extendedInMemoryUserDetailManager("password");
        UserDetails userDetails = userDetailsManager.loadUserByUsername("userName");
        assertEquals("{noop}password", userDetails.getPassword());
        //idempotency check
        userDetails = userDetailsManager.loadUserByUsername("userName");
        assertEquals("{noop}password", userDetails.getPassword());

    }

    private ExtendedInMemoryUserDetailsManager extendedInMemoryUserDetailManager(String password) {
        List<RemoteUser> remoteUsers = List.of(new RemoteUser(
                "userName",
                password,
                "institutionGUID",
                "schacHome",
                List.of("scope")
        ));
        return new ExtendedInMemoryUserDetailsManager(remoteUsers);
    }
}