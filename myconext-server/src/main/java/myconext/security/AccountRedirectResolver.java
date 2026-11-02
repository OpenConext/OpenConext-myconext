package myconext.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
