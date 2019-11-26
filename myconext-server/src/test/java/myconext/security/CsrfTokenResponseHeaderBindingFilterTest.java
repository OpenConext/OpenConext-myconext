package myconext.security;

import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.csrf.DefaultCsrfToken;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class CsrfTokenResponseHeaderBindingFilterTest {

    private CsrfTokenResponseHeaderBindingFilter subject = new CsrfTokenResponseHeaderBindingFilter();

    @Test
    public void doFilterInternal() throws ServletException, IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();
        String token = UUID.randomUUID().toString();
        DefaultCsrfToken csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", token);

        request.setAttribute("_csrf", csrfToken);
        subject.doFilterInternal(request, response, new MockFilterChain());

        assertEquals(token, response.getHeader(csrfToken.getHeaderName()));
    }
}