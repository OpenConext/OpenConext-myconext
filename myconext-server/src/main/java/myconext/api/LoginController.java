package myconext.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static myconext.security.GuestIdpAuthenticationRequestFilter.REGISTER_MODUS_COOKIE_NAME;

@RestController
public class LoginController {

    private static final Log LOG = LogFactory.getLog(LoginController.class);

    private boolean secureCookie;

    private final Map<String, Object> config = new HashMap<>();

    public LoginController(@Value("${base_path}") String basePath,
                           @Value("${base_domain}") String baseDomain,
                           @Value("${my_conext_url}") String myConextUrl,
                           @Value("${onegini_entity_id}") String oneGiniEntityId,
                           @Value("${guest_idp_entity_id}") String guestIdpEntityId,
                           @Value("${email.magic-link-url}") String magicLinkUrl,
                           @Value("${domain}") String domain,
                           @Value("${secure_cookie}") boolean secureCookie,
                           @Value("${idp_redirect_url}") String idpBaseUrl,
                           @Value("${sp_redirect_url}") String spBaseUrl,
                           @Value("${feature.webauthn}") boolean featureWebAuthn,
                           @Value("${oidc-token-api.enabled}") boolean featureOidcTokenAPI)  {
        this.config.put("loginUrl", basePath + "/login");
        this.config.put("baseDomain", baseDomain);
        this.config.put("migrationUrl", String.format("%s/Shibboleth.sso/Login?entityID=%s&target=/migration", myConextUrl, oneGiniEntityId));
        this.config.put("magicLinkUrl", magicLinkUrl);
        this.config.put("eduIDLoginUrl", String.format("%s/Shibboleth.sso/Login?entityID=%s", myConextUrl, guestIdpEntityId));
        this.config.put("eduIDRegisterUrl", String.format("%s/register", idpBaseUrl));
        this.config.put("eduIDDoLoginUrl", String.format("%s/doLogin", idpBaseUrl));
        this.config.put("eduIDWebAuthnUrl", String.format("%s/webauthn", idpBaseUrl));
        this.config.put("eduIDWebAuthnRedirectSpUrl", String.format("%s/webauthn", spBaseUrl));
        this.config.put("domain", domain);
        this.config.put("featureWebAuthn", featureWebAuthn);
        this.config.put("featureOidcTokenAPI", featureOidcTokenAPI);
        this.secureCookie = secureCookie;
    }

    @GetMapping("/register")
    public void register(@RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
                         @RequestParam(value = "location", required = false) String location,
                         HttpServletResponse response) throws IOException {
        response.setHeader("Set-Cookie", REGISTER_MODUS_COOKIE_NAME + "=true; SameSite=None" + (secureCookie ? "; Secure" : ""));
        String redirectLocation = StringUtils.hasText(location) ? location : this.config.get("eduIDLoginUrl") + "&lang=" + lang;
        response.sendRedirect(redirectLocation);
    }

    @GetMapping("/doLogin")
    public void doLogin(@RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
                         @RequestParam(value = "location", required = false) String location,
                         HttpServletResponse response) throws IOException {
        response.setHeader("Set-Cookie", REGISTER_MODUS_COOKIE_NAME + "=; expires=Thu, 01 Jan 1970 00:00:00 GMT; SameSite=None" + (secureCookie ? "; Secure" : ""));
        String redirectLocation = StringUtils.hasText(location) ? location : this.config.get("eduIDLoginUrl") + "&lang=" + lang;
        response.sendRedirect(redirectLocation);
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        return config;
    }

}
