package myconext.shibboleth.mock;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import myconext.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.HashMap;

public class MockShibbolethFilter extends GenericFilterBean {

    private final boolean serviceDeskRoleAutoProvisioning;

//    private String uid = "7287aa59-01c9-4b5c-8a10-c21e82090b52";
//    public String email = "ok@ok.com";

    private final String host;
    private String uid = "1234567890";
    public String email = "jdoe@example.com";


    public MockShibbolethFilter(boolean serviceDeskRoleAutoProvisioning, String activeHost) {
        this.serviceDeskRoleAutoProvisioning = serviceDeskRoleAutoProvisioning;
        this.host = activeHost;
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
        SetHeader wrapper = new SetHeader(servletRequest);
        wrapper.setHeader(ShibbolethPreAuthenticatedProcessingFilter.SHIB_SCHAC_HOME_ORGANIZATION, "surfguest.nl");
        wrapper.setHeader(ShibbolethPreAuthenticatedProcessingFilter.SHIB_UID, uid);
        wrapper.setHeader(ShibbolethPreAuthenticatedProcessingFilter.SHIB_EMAIL, email);
        wrapper.setHeader(ShibbolethPreAuthenticatedProcessingFilter.SHIB_GIVEN_NAME, "John");
        wrapper.setHeader(ShibbolethPreAuthenticatedProcessingFilter.SHIB_SUR_NAME, "Doe");
        wrapper.setHeader("host", host);
        if (serviceDeskRoleAutoProvisioning) {
            wrapper.setHeader(ShibbolethPreAuthenticatedProcessingFilter.SHIB_MEMBERSHIPS, "role3");
        }
        filterChain.doFilter(wrapper, response);
    }
}
