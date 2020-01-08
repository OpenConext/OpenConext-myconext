package myconext.shibboleth.mock;

import myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class MockShibbolethFilter extends GenericFilterBean {

    private Environment environment;

    private static String uid = "1234567890";

    public MockShibbolethFilter(Environment environment) {
        this.environment = environment;
    }

    private static class SetHeader extends HttpServletRequestWrapper {

        private final HashMap<String, String> headers;

        public SetHeader(HttpServletRequest request) {
            super(request);
            this.headers = new HashMap<>();
        }

        public void setHeader(String name, String value) {
            this.headers.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            return headers.getOrDefault(name, super.getHeader(name));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;

        if (environment.acceptsProfiles(Profiles.of("test")) ||
                (environment.acceptsProfiles(Profiles.of("dev")) && ((HttpServletRequest) request).getRequestURI().startsWith("/startSSO"))) {
            SetHeader wrapper = new SetHeader(servletRequest);
            wrapper.setHeader(ShibbolethPreAuthenticatedProcessingFilter.SHIB_SCHAC_HOME_ORGANIZATION, "surfguest.nl");
            wrapper.setHeader(ShibbolethPreAuthenticatedProcessingFilter.SHIB_UID, uid);
            wrapper.setHeader(ShibbolethPreAuthenticatedProcessingFilter.SHIB_EMAIL, "jdoe@example.com");
            wrapper.setHeader(ShibbolethPreAuthenticatedProcessingFilter.SHIB_GIVEN_NAME, "John");
            wrapper.setHeader(ShibbolethPreAuthenticatedProcessingFilter.SHIB_SUR_NAME, "Doe");
            filterChain.doFilter(wrapper, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
