package myconext.security;

import jakarta.servlet.http.HttpServletRequest;
import oidc.security.AuthorizationRequestCustomizer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class AppAwareAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public AppAwareAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                repo, "/oauth2/authorization"
        );
        this.delegate.setAuthorizationRequestCustomizer(new AuthorizationRequestCustomizer());
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        // Only initiate an authorization request when the URI is the login-initiation endpoint.
        // Without this guard, every request through this filter chain (e.g. /config) would be
        // turned into an OAuth2 redirect because the 2-arg delegate.resolve does not check the path.
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.endsWith("config")) {
            return null;
        }
        String registrationId;
        if (path.startsWith("/myconext/api/sp/login")) {
            registrationId = "oidcng";
        } else {
            registrationId = "oidcng-gg";
        }
        OAuth2AuthorizationRequest resolve = delegate.resolve(request, registrationId);
        return resolve;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
        return delegate.resolve(request, registrationId);
    }

    private String resolveRegistrationId(HttpServletRequest request) {
        String host = request.getServerName();
        if (host.startsWith("app1")) { // todo
            return "oidcng";
        }
        return "oidcng";
    }

}
