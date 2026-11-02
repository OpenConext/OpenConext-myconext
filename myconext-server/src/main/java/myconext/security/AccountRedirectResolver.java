package myconext.security;

import jakarta.servlet.http.HttpServletRequest;
import myconext.model.SamlAuthenticationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * Decides whether a redirect in the login flow must go to the legacy account-gui ({@code account_redirect_url})
 * or to login-gui ({@code login_gui_redirect_url}). Both run at the same time on different (sub)domains, and the
 * server is reachable on both. The choice is made on the host the request came in on, which is the host of the
 * SSO endpoint in the IdP metadata. With {@code server.forward-headers-strategy=framework} this host is the one
 * from the X-Forwarded-Host / X-Forwarded-Proto headers set by the load-balancer. Only the two configured hosts
 * can ever be returned, so a spoofed host header can not lead to an open redirect.
 */
@Component
public class AccountRedirectResolver {

    private final String accountRedirectUrl;
    private final String loginGuiRedirectUrl;
    private final String loginGuiAuthority;

    public AccountRedirectResolver(@Value("${account_redirect_url}") String accountRedirectUrl,
                                   @Value("${login_gui_redirect_url:}") String loginGuiRedirectUrl) {
        this.accountRedirectUrl = accountRedirectUrl;
        this.loginGuiRedirectUrl = loginGuiRedirectUrl;
        this.loginGuiAuthority = StringUtils.hasText(loginGuiRedirectUrl) ? authority(URI.create(loginGuiRedirectUrl)) : null;
    }

    public String resolve(HttpServletRequest request) {
        if (loginGuiAuthority == null) {
            return accountRedirectUrl;
        }
        return loginGuiAuthority.equalsIgnoreCase(authority(request)) ? loginGuiRedirectUrl : accountRedirectUrl;
    }

    /**
     * Moves a configured absolute URL (e.g. the magic link) to the host of the GUI that handles this request, keeping
     * its path and query. For the legacy account-gui the URL is returned untouched.
     */
    public String rebase(HttpServletRequest request, String url) {
        if (loginGuiAuthority == null || !StringUtils.hasText(url) || !loginGuiRedirectUrl.equals(resolve(request))) {
            return url;
        }
        return moveToLoginGui(url);
    }

    /**
     * The account GUI that started the given login flow, as remembered in {@link SamlAuthenticationRequest#getGuiRedirectUrl()}.
     * For callbacks from external parties, that arrive on the host registered with them and not on the host of the GUI.
     * Only one of the two configured URLs is returned, whatever is stored.
     */
    public String resolve(SamlAuthenticationRequest samlAuthenticationRequest) {
        return isLoginGui(samlAuthenticationRequest) ? loginGuiRedirectUrl : accountRedirectUrl;
    }

    public String rebase(SamlAuthenticationRequest samlAuthenticationRequest, String url) {
        return isLoginGui(samlAuthenticationRequest) && StringUtils.hasText(url) ? moveToLoginGui(url) : url;
    }

    /**
     * A short marker of the GUI of this request to carry in the OIDC 'state' of flows, for callbacks that can not find
     * the flow (expired) or are an error response from the other party.
     */
    public String marker(HttpServletRequest request) {
        return loginGuiRedirectUrl != null && loginGuiRedirectUrl.equals(resolve(request)) ? "login" : "account";
    }

    public String resolveMarker(String marker) {
        return loginGuiAuthority != null && "login".equals(marker) ? loginGuiRedirectUrl : accountRedirectUrl;
    }

    private boolean isLoginGui(SamlAuthenticationRequest samlAuthenticationRequest) {
        return loginGuiAuthority != null && loginGuiRedirectUrl.equals(samlAuthenticationRequest.getGuiRedirectUrl());
    }

    private String moveToLoginGui(String url) {
        URI base = URI.create(loginGuiRedirectUrl);
        return UriComponentsBuilder.fromUriString(url).scheme(base.getScheme()).host(base.getHost()).port(base.getPort()).build().toUriString();
    }

    /**
     * Same as {@link #rebase(HttpServletRequest, String)} for the request bound to the current thread. Outside a
     * request (e.g. scheduled jobs) the URL is returned untouched.
     */
    public String rebase(String url) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servletRequestAttributes ?
                rebase(servletRequestAttributes.getRequest(), url) : url;
    }

    private String authority(HttpServletRequest request) {
        return authority(request.getServerName(), request.getServerPort(), request.getScheme());
    }

    private String authority(URI uri) {
        return authority(uri.getHost(), uri.getPort(), uri.getScheme());
    }

    private String authority(String host, int port, String scheme) {
        boolean defaultPort = port <= 0 || ("https".equalsIgnoreCase(scheme) && port == 443)
                || ("http".equalsIgnoreCase(scheme) && port == 80);
        return defaultPort ? host : host + ":" + port;
    }
}
