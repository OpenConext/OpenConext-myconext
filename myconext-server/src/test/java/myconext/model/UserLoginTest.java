package myconext.model;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserLoginTest {

    @Test
    public void inetAddress() {
        UserLogin userLogin = new UserLogin(new User(), Collections.singletonMap("x-forwarded-for", "1.2.3.4 , 5.6.7.8"));
        assertTrue(userLogin.getIpAddress().equals("surf.nl" ) || userLogin.getIpAddress().equals("1.2.3.4"));
    }

    @Test
    public void forwardedFor() {
        UserLogin userLogin = new UserLogin(new User(), Collections.singletonMap("ipAddress", "0.0.0.1"));
        assertEquals("0.0.0.1", userLogin.getIpAddress());
    }
}