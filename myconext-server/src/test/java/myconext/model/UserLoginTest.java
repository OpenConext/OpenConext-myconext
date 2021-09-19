package myconext.model;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class UserLoginTest {

    @Test
    public void inetAddress() {
        UserLogin userLogin = new UserLogin(new User(), Collections.singletonMap("x-forwarded-for", "145.100.190.243 , 145.100.190.999"));
        assertEquals("surf.nl", userLogin.getIpAddress());
    }

    @Test
    public void forwardedFor() {
        UserLogin userLogin = new UserLogin(new User(), Collections.singletonMap("ipAddress", "0.0.0.1"));
        assertEquals("0.0.0.1", userLogin.getIpAddress());
    }
}