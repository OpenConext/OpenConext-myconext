package myconext.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AccountRedirectResolverTest {

    private final AccountRedirectResolver resolver =
            new AccountRedirectResolver("https://login.test2.surfconext.nl", "https://login-nieuw.test2.surfconext.nl");

    private MockHttpServletRequest request(String scheme, String host, int port) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setScheme(scheme);
        request.setServerName(host);
        request.setServerPort(port);
        return request;
    }

    @Test
    void oldHost() {
        assertEquals("https://login.test2.surfconext.nl", resolver.resolve(request("https", "login.test2.surfconext.nl", 443)));
    }

    @Test
    void newHost() {
        assertEquals("https://login-nieuw.test2.surfconext.nl", resolver.resolve(request("https", "login-nieuw.test2.surfconext.nl", 443)));
    }

    @Test
    void unknownHostFallsBackToOld() {
        assertEquals("https://login.test2.surfconext.nl", resolver.resolve(request("https", "evil.example.com", 443)));
    }

    @Test
    void localhostPorts() {
        AccountRedirectResolver local = new AccountRedirectResolver("http://localhost:3000", "http://localhost:3004");
        assertEquals("http://localhost:3004", local.resolve(request("http", "localhost", 3004)));
        assertEquals("http://localhost:3000", local.resolve(request("http", "localhost", 3000)));
    }

    @Test
    void notConfigured() {
        AccountRedirectResolver old = new AccountRedirectResolver("http://localhost:3000", "");
        assertEquals("http://localhost:3000", old.resolve(request("http", "localhost", 3004)));
    }
}
