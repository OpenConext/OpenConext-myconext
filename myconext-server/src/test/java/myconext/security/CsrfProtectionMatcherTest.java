package myconext.security;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CsrfProtectionMatcherTest {

    private CsrfProtectionMatcher subject = new CsrfProtectionMatcher();

    @Test
    public void matches() {
        assertFalse(subject.matches(servletRequest("GET", "")));
        assertFalse(subject.matches(servletRequest("GET", "/myconext/api/sp/action")));
        assertFalse(subject.matches(servletRequest("POST", "/myconext/api/idp/action")));

        assertTrue(subject.matches(servletRequest("POST", "/myconext/api/sp/action")));
    }

    private MockHttpServletRequest servletRequest(String method, String servletPath) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, servletPath);
        request.setServletPath(servletPath);
        return request;
    }
}