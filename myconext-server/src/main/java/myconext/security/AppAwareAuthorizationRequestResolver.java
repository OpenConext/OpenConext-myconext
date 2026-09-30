package myconext.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Thin wrapper around {@link DefaultOAuth2AuthorizationRequestResolver} that adds
 * {@code prompt=login} when the original (saved) request carried {@code force=}.
 * <p>
 * This customizer used to be {@code oidc.security.AuthorizationRequestCustomizer} from
 * {@code org.openconext:openconext-oidc-client}, but that artifact is compiled against
 * Spring Security 6 and its {@code OAuth2AuthorizationRequest.Builder.additionalParameters}
 * call became binary-incompatible under Spring Security 7 (covariant return type change),
 * throwing {@code NoSuchMethodError} on every OAuth2 login. Reimplemented inline here since
 * no Spring-Security-7-compiled release of that artifact exists yet; revert to the shared
 * artifact once one is published. The registrationId is taken from the request URL
 * ({@code /oauth2/authorization/{registrationId}}); choosing which registration to use for
 * an unauthenticated request is handled by the {@code AuthenticationEntryPoint} in
 * {@link SecurityConfiguration}.
 */
@Component
public class AppAwareAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public AppAwareAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(
                repo, "/oauth2/authorization"
        );
        this.delegate.setAuthorizationRequestCustomizer(builder -> builder.additionalParameters(this::addPromptLoginIfForced));
    }

    private void addPromptLoginIfForced(java.util.Map<String, Object> params) {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        DefaultSavedRequest savedRequest = (DefaultSavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        if (savedRequest == null) {
            return;
        }
        String[] force = savedRequest.getParameterValues("force");
        if (force != null) {
            params.put("prompt", "login");
        }
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
