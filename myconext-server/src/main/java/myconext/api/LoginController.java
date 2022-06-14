package myconext.api;

import myconext.exceptions.UserNotFoundException;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static myconext.security.GuestIdpAuthenticationRequestFilter.REGISTER_MODUS_COOKIE_NAME;

@RestController
public class LoginController {

    private final boolean secureCookie;

    private final Map<String, Object> config = new HashMap<>();
    private final UserRepository userRepository;

    public LoginController(UserRepository userRepository,
                           @Value("${base_path}") String basePath,
                           @Value("${base_domain}") String baseDomain,
                           @Value("${my_conext_url}") String myConextUrl,
                           @Value("${onegini_entity_id}") String oneGiniEntityId,
                           @Value("${continue_after_login_url}") String continueAfterLoginUrl,
                           @Value("${email.magic-link-url}") String magicLinkUrl,
                           @Value("${domain}") String domain,
                           @Value("${secure_cookie}") boolean secureCookie,
                           @Value("${idp_redirect_url}") String idpBaseUrl,
                           @Value("${sp_redirect_url}") String spBaseUrl,
                           @Value("${feature.webauthn}") boolean featureWebAuthn,
                           @Value("${feature.connections}") boolean featureConnections,
                           @Value("${feature.warning_educational_email_domain}") boolean featureWarningEducationalEmailDomain,
                           @Value("${feature.use_deny_allow_list.allow_enabled}") boolean featureAllowList,
                           @Value("${feature.use_external_validation}") boolean useExternalValidation,
                           @Value("${oidc-token-api.enabled}") boolean featureOidcTokenAPI,
                           @Value("${email_spam_threshold_seconds}") long emailSpamThresholdSeconds) {
        this.config.put("loginUrl", basePath + "/login");
        this.config.put("continueAfterLoginUrl", continueAfterLoginUrl);
        this.config.put("baseDomain", baseDomain);
        this.config.put("migrationUrl", String.format("%s/Shibboleth.sso/Login?entityID=%s&target=/migration", myConextUrl, oneGiniEntityId));
        this.config.put("magicLinkUrl", magicLinkUrl);
        this.config.put("idpBaseUrl", idpBaseUrl);
        this.config.put("spBaseUrl", spBaseUrl);
        this.config.put("eduIDWebAuthnUrl", String.format("%s/webauthn", idpBaseUrl));
        this.config.put("eduIDWebAuthnRedirectSpUrl", String.format("%s/security", spBaseUrl));
        this.config.put("domain", domain);
        this.config.put("featureWebAuthn", featureWebAuthn);
        this.config.put("featureWarningEducationalEmailDomain", featureWarningEducationalEmailDomain);
        this.config.put("featureAllowList", featureAllowList);
        this.config.put("featureOidcTokenAPI", featureOidcTokenAPI);
        this.config.put("featureConnections", featureConnections);
        this.config.put("useExternalValidation", useExternalValidation);
        this.config.put("emailSpamThresholdSeconds", emailSpamThresholdSeconds);
        this.secureCookie = secureCookie;
        this.userRepository = userRepository;
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        return config;
    }

    @GetMapping("/register/{id}")
    public void register(@PathVariable("id") String id,
                         HttpServletResponse response) throws IOException {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        Cookie loginPreferenceCookie = new Cookie("login_preference", "useApp");
        loginPreferenceCookie.setMaxAge(365 * 60 * 60 * 24);
        loginPreferenceCookie.setSecure(secureCookie);
        loginPreferenceCookie.setPath("/");

        Cookie usernameCookie = new Cookie("username", user.getEmail());
        usernameCookie.setMaxAge(365 * 60 * 60 * 24);
        usernameCookie.setSecure(secureCookie);
        usernameCookie.setPath("/");

        response.addCookie(loginPreferenceCookie);
        response.addCookie(usernameCookie);

        String redirectLocation = this.config.get("spBaseUrl") + "/security";
        response.sendRedirect(redirectLocation);
    }

}
