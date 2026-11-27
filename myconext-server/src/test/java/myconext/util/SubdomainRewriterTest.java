package myconext.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SubdomainRewriterTest {

    @Test
    void appendNewToHostRewritesLoginAndMijn() {
        assertEquals("login-nieuw.test2.eduid.nl", SubdomainRewriter.appendNewToHost("login.test2.eduid.nl"));
        assertEquals("mijn-nieuw.test2.eduid.nl", SubdomainRewriter.appendNewToHost("mijn.test2.eduid.nl"));
    }

    @Test
    void appendNewToHostLeavesOtherHostsUnchanged() {
        assertEquals("localhost", SubdomainRewriter.appendNewToHost("localhost"));
        assertEquals("servicedesk.test2.eduid.nl", SubdomainRewriter.appendNewToHost("servicedesk.test2.eduid.nl"));
        assertEquals("test2.eduid.nl", SubdomainRewriter.appendNewToHost("test2.eduid.nl"));
    }

    @Test
    void moveToBaseReplacesSchemeHostAndPortKeepingPathAndQuery() {
        assertEquals("http://localhost:3004/saml/guest-idp/magic",
                SubdomainRewriter.moveToBase("http://localhost:8081/saml/guest-idp/magic", "http://localhost:3004"));
        assertEquals("https://login.test2.eduid.nl/saml/guest-idp/magic?a=b",
                SubdomainRewriter.moveToBase("http://localhost:8081/saml/guest-idp/magic?a=b", "https://login.test2.eduid.nl"));
    }

    @Test
    void moveToBaseReturnsUrlUnchangedWhenNothingToMove() {
        String url = "http://localhost:8081/saml/guest-idp/magic";
        assertEquals(url, SubdomainRewriter.moveToBase(url, ""));
        assertEquals(url, SubdomainRewriter.moveToBase(url, "not a url"));
        assertEquals("", SubdomainRewriter.moveToBase("", "http://localhost:3004"));
        assertNull(SubdomainRewriter.moveToBase(null, "http://localhost:3004"));
    }

    @Test
    void appendNewToHostHandlesBlank() {
        assertEquals("", SubdomainRewriter.appendNewToHost(""));
        assertNull(SubdomainRewriter.appendNewToHost(null));
    }
}
