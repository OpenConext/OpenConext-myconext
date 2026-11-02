package myconext.security;

import myconext.model.SamlAuthenticationRequest;
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

    @Test
    void rebase() {
        String magic = "https://login.test2.surfconext.nl/saml/guest-idp/magic";
        assertEquals("https://login-nieuw.test2.surfconext.nl/saml/guest-idp/magic",
                resolver.rebase(request("https", "login-nieuw.test2.surfconext.nl", 443), magic));
        assertEquals(magic, resolver.rebase(request("https", "login.test2.surfconext.nl", 443), magic));
    }

    @Test
    void savedGuiOfFlow() {
        SamlAuthenticationRequest samlAuthenticationRequest = new SamlAuthenticationRequest();
        String magic = "https://login.test2.surfconext.nl/saml/guest-idp/magic";
        assertEquals("https://login.test2.surfconext.nl", resolver.resolve(samlAuthenticationRequest));
        assertEquals(magic, resolver.rebase(samlAuthenticationRequest, magic));

        samlAuthenticationRequest.setGuiRedirectUrl("https://login-nieuw.test2.surfconext.nl");
        assertEquals("https://login-nieuw.test2.surfconext.nl", resolver.resolve(samlAuthenticationRequest));
        assertEquals("https://login-nieuw.test2.surfconext.nl/saml/guest-idp/magic", resolver.rebase(samlAuthenticationRequest, magic));

        samlAuthenticationRequest.setGuiRedirectUrl("https://evil.example.com");
        assertEquals("https://login.test2.surfconext.nl", resolver.resolve(samlAuthenticationRequest));
    }

    @Test
    void marker() {
        assertEquals("login", resolver.marker(request("https", "login-nieuw.test2.surfconext.nl", 443)));
        assertEquals("account", resolver.marker(request("https", "login.test2.surfconext.nl", 443)));
        assertEquals("https://login-nieuw.test2.surfconext.nl", resolver.resolveMarker("login"));
        assertEquals("https://login.test2.surfconext.nl", resolver.resolveMarker("evil"));
        assertEquals("https://login.test2.surfconext.nl", resolver.resolveMarker(null));
    }
}
