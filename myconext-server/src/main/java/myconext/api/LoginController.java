package myconext.api;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import myconext.exceptions.UserNotFoundException;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static myconext.security.GuestIdpAuthenticationRequestFilter.REGISTER_MODUS_COOKIE_NAME;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@RestController
@Hidden
public class LoginController {

    private static final Log LOG = LogFactory.getLog(LoginController.class);

    private final boolean secureCookie;

    private final Map<String, Object> config = new HashMap<>();
    private final UserRepository userRepository;
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final SecurityContextRepository securityContextRepository;

    public LoginController(UserRepository userRepository,
                           AuthenticationRequestRepository authenticationRequestRepository,
                           SecurityContextRepository securityContextRepository,
                           @Value("${base_path}") String basePath,
                           @Value("${base_domain}") String baseDomain,
                           @Value("${my_conext_url}") String myConextUrl,
                           @Value("${guest_idp_entity_id}") String guestIdpEntityId,
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
                           @Value("${feature.default_remember_me}") boolean featureDefaultRememberMe,
                           @Value("${oidc-token-api.enabled}") boolean featureOidcTokenAPI,
                           @Value("${feature.create_eduid_institution_enabled}") boolean createEduIDInstitutionEnabled,
                           @Value("${feature.create_eduid_institution_landing}") boolean createEduIDInstitutionLanding,
                           @Value("${email_spam_threshold_seconds}") long emailSpamThresholdSeconds,
                           @Value("${linked_accounts.expiry-duration-days-non-validated}") long expirationNonValidatedDurationDays,
                           @Value("${mobile_app_redirect}") String mobileAppRedirect,
                           @Value("${feature.id_verify}") boolean idVerify
    ) {
        this.config.put("basePath", basePath);
        this.config.put("loginUrl", basePath + "/login");
        this.config.put("continueAfterLoginUrl", continueAfterLoginUrl);
        this.config.put("baseDomain", baseDomain);
        this.config.put("magicLinkUrl", magicLinkUrl);
        this.config.put("idpBaseUrl", idpBaseUrl);
        this.config.put("spBaseUrl", spBaseUrl);
        this.config.put("eduIDWebAuthnUrl", String.format("%s/webauthn", idpBaseUrl));
        this.config.put("eduIDLoginUrl", String.format("%s/Shibboleth.sso/Login?entityID=%s", myConextUrl, guestIdpEntityId));
        this.config.put("eduIDWebAuthnRedirectSpUrl", String.format("%s/security", spBaseUrl));
        this.config.put("domain", domain);
        this.config.put("featureWebAuthn", featureWebAuthn);
        this.config.put("featureWarningEducationalEmailDomain", featureWarningEducationalEmailDomain);
        this.config.put("featureAllowList", featureAllowList);
        this.config.put("featureOidcTokenAPI", featureOidcTokenAPI);
        this.config.put("featureConnections", featureConnections);
        this.config.put("featureDefaultRememberMe", featureDefaultRememberMe);
        this.config.put("emailSpamThresholdSeconds", emailSpamThresholdSeconds);
        this.config.put("createEduIDInstitutionEnabled", createEduIDInstitutionEnabled);
        this.config.put("createEduIDInstitutionLanding", createEduIDInstitutionLanding);
        this.config.put("expirationNonValidatedDurationDays", expirationNonValidatedDurationDays);
        this.config.put("mobileAppRedirect", mobileAppRedirect);
        this.config.put("featureIdVerify", idVerify);
        this.secureCookie = secureCookie;
        this.userRepository = userRepository;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/config")
    public Map<String, Object> config() {
        return config;
    }

    @GetMapping("/register")
    public void register(@RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
                         @RequestParam(value = "location", required = false) String location,
                         HttpServletResponse response) throws IOException {
        doRedirect(lang, location, response, true);
    }

    @GetMapping("/register/{enrollmentVerificationKey}")
    public void register(@PathVariable("enrollmentVerificationKey") String enrollmentVerificationKey,
                         HttpServletResponse response) throws IOException {

        User user = userRepository.findUserByEnrollmentVerificationKey(enrollmentVerificationKey)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setEnrollmentVerificationKey(null);
        userRepository.save(user);

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

    @GetMapping("/servicedesk/{id}")
    @Hidden
    public ResponseEntity redirectToSPServiceDeskHook(@PathVariable("id") String id,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {
        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(id);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            String idpBaseUrl = (String) this.config.get("idpBaseUrl");
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(idpBaseUrl + "/expired")).build();
        }
        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        this.securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

        String redirectUrl = String.format("%s/personal?servicedesk=start", this.config.get("spBaseUrl"));

        LOG.info(String.format("User %s logged in to process servicedesk request. Redirecting to %s", user.getEmail(), redirectUrl));

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

    @GetMapping("/doLogin")
    public void doLogin(@RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
                        @RequestParam(value = "location", required = false) String location,
                        @RequestParam(value = "register", required = false, defaultValue = "true") String register,
                        HttpServletResponse response) throws IOException {
        doRedirect(lang, location, response, Boolean.valueOf(register));
    }

    private void doRedirect(String lang, String location, HttpServletResponse response, boolean register) throws IOException {
        if (register) {
            String cookieValue = String.format("%s=true; Max-Age=%s; SameSite=None%s", REGISTER_MODUS_COOKIE_NAME, 60 * 10, secureCookie ? "; Secure" : "");
            response.setHeader("Set-Cookie", cookieValue);
        }
        String redirectLocation = StringUtils.hasText(location) ? location : this.config.get("eduIDLoginUrl") + "&lang=" + lang;

        LOG.info(String.format("Redirecting to %s", redirectLocation));

        response.sendRedirect(redirectLocation);
    }

    @GetMapping("/doLogout")
    public void doLogout(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestParam(value = "param") String param) throws IOException {
        if (param.contains("delete")) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                Arrays.asList(cookies).forEach(cookie -> {
                    cookie.setMaxAge(0);
                    cookie.setSecure(true);
                    cookie.setValue("");
                    response.addCookie(cookie);
                });
            }
        }
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
        String redirectLocation = String.format("%s/landing?%s", this.config.get("spBaseUrl"), param);

        LOG.info(String.format("Logout and redirect to %s", redirectLocation));

        response.sendRedirect(redirectLocation);
    }

    @GetMapping("create-from-institution-login")
    public void createFromInstitutionLogin(HttpServletRequest request,
                                           HttpServletResponse response,
                                           @RequestParam(value = "key") String key) throws IOException {
        String redirectUrl = String.format("%s/security", this.config.get("spBaseUrl"));
        doCreateUserFromInstitutionKey(request, response, key, redirectUrl);
    }

    @GetMapping("/mobile/api/create-from-mobile-api")
    public void createFromMobileApi(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value = "h") String hash) throws IOException {
        String redirectUrl = String.format("%s/client/mobile/created", this.config.get("idpBaseUrl"));
        doCreateUserFromInstitutionKey(request, response, hash, redirectUrl);
    }

    private void doCreateUserFromInstitutionKey(HttpServletRequest request,
                                                HttpServletResponse response,
                                                String key,
                                                String redirectUrl) throws IOException {
        User user = userRepository.findUserByCreateFromInstitutionKey(key)
                .orElseThrow(() -> new UserNotFoundException("User by createFromInstitutionKey not found"));
        boolean newUser = user.isNewUser();
        user.setNewUser(false);
        user.setCreateFromInstitutionKey(null);
        userRepository.save(user);

        Cookie usernameCookie = new Cookie("username", user.getEmail());
        usernameCookie.setMaxAge(365 * 60 * 60 * 24);
        usernameCookie.setSecure(secureCookie);
        usernameCookie.setPath("/");
        response.addCookie(usernameCookie);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        this.securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

        String redirectLocation = redirectUrl + String.format("?new=%s", newUser ? "true" : "false");

        LOG.info(String.format("User %s create from institutionKey. Redirecting to %s", user.getEmail(), redirectLocation));

        response.sendRedirect(redirectLocation);
    }
}
