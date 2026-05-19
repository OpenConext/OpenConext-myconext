package myconext.security;

import jakarta.servlet.http.HttpServletRequest;
import oidc.security.AuthorizationRequestCustomizer;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

/**
 * Thin wrapper around {@link DefaultOAuth2AuthorizationRequestResolver} that installs the
 * OpenConext {@link AuthorizationRequestCustomizer} (adds {@code prompt=login} when the
 * original request carried {@code force=}). The registrationId is taken from the request
 * URL ({@code /oauth2/authorization/{registrationId}}); choosing which registration to use
 * for an unauthenticated request is handled by the {@code AuthenticationEntryPoint} in
 * {@link SecurityConfiguration}.
 */
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
        return delegate.resolve(request);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
        return delegate.resolve(request, registrationId);
    }
}
