package myconext.api;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import myconext.config.CreateFromInstitutionProperties;
import myconext.exceptions.UserNotFoundException;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.UserRepository;
import myconext.security.SecurityConfiguration.InternalSecurityConfigurationAdapter;
import myconext.util.CreateFromInstitutionReturnUrlSupport;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static myconext.security.GuestIdpAuthenticationRequestFilter.REGISTER_MODUS_COOKIE_NAME;

@RestController
@Hidden
public class LoginController {

    private static final Log LOG = LogFactory.getLog(LoginController.class);

    private final boolean secureCookie;

    private final Map<String, Object> config = new HashMap<>();
    private final UserRepository userRepository;
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final SecurityContextRepository securityContextRepository;
    private final List<String> createFromInstitutionAllowedReturnDomains;
    private final String myconextRedirectUrl;
    private final String servicedeskRedirectUrl;
    private final String myconextLoginUrl;

    public LoginController(UserRepository userRepository,
                           AuthenticationRequestRepository authenticationRequestRepository,
                           SecurityContextRepository securityContextRepository,
                           @Value("${myconext_base_path}") String myconextBasePath,
                           @Value("${servicedesk_base_path}") String servicedeskBasePath,
                           @Value("${base_domain}") String baseDomain,
                           @Value("${continue_after_login_url}") String continueAfterLoginUrl,
                           @Value("${email.magic-link-url}") String magicLinkUrl,
                           @Value("${secure_cookie}") boolean secureCookie,
                           @Value("${account_redirect_url}") String accountRedirectUrl,
                           @Value("${myconext_redirect_url}") String myconextRedirectUrl,
                           @Value("${servicedesk_redirect_url}") String servicedeskRedirectUrl,
                           @Value("${feature.webauthn}") boolean featureWebAuthn,
                           @Value("${feature.connections}") boolean featureConnections,
                           @Value("${feature.warning_educational_email_domain}") boolean featureWarningEducationalEmailDomain,
                           @Value("${feature.use_deny_allow_list.allow_enabled}") boolean featureAllowList,
                           @Value("${feature.default_remember_me}") boolean featureDefaultRememberMe,
                           @Value("${oidc-token-api.enabled}") boolean featureOidcTokenAPI,
                           @Value("${feature.create_eduid_institution_enabled}") boolean createEduIDInstitutionEnabled,
                           @Value("${feature.create_eduid_institution_landing}") boolean createEduIDInstitutionLanding,
                           @Value("${feature.captcha_enabled}") boolean captchaEnabled,
                           @Value("${captcha.sitekey}") String captchaSiteKey,
                           @Value("${email_spam_threshold_seconds}") long emailSpamThresholdSeconds,
                           @Value("${linked_accounts.expiry-duration-days-non-validated}") long expirationNonValidatedDurationDays,
                           @Value("${mobile_app_redirect}") String mobileAppRedirect,
                           @Value("${feature.id_verify}") boolean idVerify,
                           @Value("${feature.service_desk_active}") boolean serviceDeskActive,
                           @Value("${feature.use_remote_creation_for_affiliation}") boolean useRemoteCreationForAffiliation,
                           @Value("${feature.enable_account_linking}") boolean enableAccountLinking,
                           @Value("${feature.use_app}") boolean useApp,
                           CreateFromInstitutionProperties createFromInstitutionProperties
    ) {
        this.config.put("myconextBasePath", myconextBasePath);
        this.config.put("loginUrl", myconextBasePath + "/auth/login");
        this.config.put("loginUrlServiceDesk", servicedeskBasePath + "/auth/login");
        this.config.put("continueAfterLoginUrl", continueAfterLoginUrl);
        this.config.put("baseDomain", baseDomain);
        this.config.put("magicLinkUrl", magicLinkUrl);
        this.config.put("accountBaseUrl", accountRedirectUrl);
        this.config.put("myconextBaseUrl", myconextRedirectUrl);
        this.config.put("servicedeskBaseUrl", servicedeskRedirectUrl);
        this.config.put("accountWebAuthUrl", String.format("%s/webauthn", accountRedirectUrl));
        this.config.put("myconextWebAuthnRedirectUrl", String.format("%s/security", myconextRedirectUrl));
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
        this.config.put("featureServiceDeskActive", serviceDeskActive);
        this.config.put("captchaEnabled", captchaEnabled);
        this.config.put("captchaSiteKey", captchaSiteKey);
        this.config.put("useRemoteCreationForAffiliation", useRemoteCreationForAffiliation);
        this.config.put("enableAccountLinking", enableAccountLinking);
        this.config.put("useApp", useApp);
        this.config.put("isAuthenticated", false);
        this.secureCookie = secureCookie;
        this.userRepository = userRepository;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.securityContextRepository = securityContextRepository;
        this.createFromInstitutionAllowedReturnDomains = createFromInstitutionProperties.getReturnUrlAllowedDomains();
        this.myconextRedirectUrl = myconextRedirectUrl;
        this.servicedeskRedirectUrl = servicedeskRedirectUrl;
        this.myconextLoginUrl = myconextBasePath + "/oauth2/authorization/my_conext";
    }

    @GetMapping("/config")
    public Map<String, Object> config(Authentication authentication) {
        Map<String, Object> result = new HashMap<>(this.config);
        result.put("isAuthenticated", authentication != null);
        return result;
    }

    @GetMapping("/auth/login")
    public View login(
            @RequestParam(value = "redirect_path", required = false) String redirectPath,
            @RequestParam(value = "registration_id") String registrationId,
            @RequestParam(value = "force", required = false, defaultValue = "false") Boolean force
    ) {
        String baseUrl = InternalSecurityConfigurationAdapter.REGISTRATION_ID_SERVICE_DESK.equals(registrationId)
                ? servicedeskRedirectUrl
                : myconextRedirectUrl;
        String target = baseUrl;
        if (StringUtils.hasText(redirectPath)) {
            String path = URLDecoder.decode(redirectPath, StandardCharsets.UTF_8);
            // Only allow internal, same-origin paths to avoid open-redirects
            if (path.startsWith("/") && !path.startsWith("//")) {
                target = baseUrl + path;
            }
        }
        LOG.debug(String.format("/login redirecting to %s", target));
        return new RedirectView(target, false);
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

        String redirectLocation = this.config.get("myconextBaseUrl") + "/security";
        response.sendRedirect(redirectLocation);
    }

    @GetMapping("/register/login-preference/{token}")
    public void registerLoginPreference(@PathVariable("token") String token,
                                        HttpServletResponse response) throws IOException {

        User user = userRepository.findUserByLoginPreferenceKey(token)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        String preference = user.getLoginPreference();
        user.setLoginPreferenceKey(null);
        user.setLoginPreference(null);
        userRepository.save(user);

        Cookie loginPreferenceCookie = new Cookie("login_preference", preference);
        loginPreferenceCookie.setMaxAge(365 * 60 * 60 * 24);
        loginPreferenceCookie.setSecure(secureCookie);
        loginPreferenceCookie.setPath("/");
        response.addCookie(loginPreferenceCookie);

        response.sendRedirect(this.config.get("myconextBaseUrl") + "/security");
    }

    @GetMapping("/servicedesk/{id}")
    @Hidden
    public ResponseEntity redirectToSPServiceDeskHook(@PathVariable("id") String id,
                                                      HttpServletRequest request,
                                                      HttpServletResponse response) {
        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(id);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            String accountBaseUrl = (String) this.config.get("accountBaseUrl");
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(accountBaseUrl + "/expired")).build();
        }
        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        this.securityContextRepository.saveContext(SecurityContextHolder.getContext(), request, response);

        String redirectUrl = String.format("%s/personal?servicedesk=start", this.config.get("myconextBaseUrl"));

        LOG.info(String.format("User %s logged in to process servicedesk request. Redirecting to %s", user.getEmail(), redirectUrl));

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirectUrl)).build();
    }

    private void doRedirect(String lang, String location, HttpServletResponse response, boolean register) throws IOException {
        if (register) {
            String cookieValue = String.format("%s=true; Max-Age=%s; SameSite=None%s", REGISTER_MODUS_COOKIE_NAME, 60 * 10, secureCookie ? "; Secure" : "");
            response.setHeader("Set-Cookie", cookieValue);
        }
        String redirectLocation = StringUtils.hasText(location) ? location : this.myconextLoginUrl + "?lang=" + lang;

        LOG.info(String.format("Redirecting to %s", redirectLocation));

        response.sendRedirect(redirectLocation);
    }

    @GetMapping("/doLogout")
    public void doLogout(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestParam(value = "param") String param,
                         @RequestParam(value = "app", required = false, defaultValue = "mijn") String app) throws IOException {
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
        String key = StringUtils.hasText(app) && "servicedesk".equalsIgnoreCase(app) ? "servicedeskBaseUrl" : "myconextBaseUrl";
        Object url = this.config.get(key);
        String redirectLocation = String.format("%s/landing?%s", url, param);

        LOG.info(String.format("Logout and redirect to %s", redirectLocation));

        response.sendRedirect(redirectLocation);
    }

    @GetMapping("create-from-institution-login")
    public void createFromInstitutionLogin(HttpServletRequest request,
                                           HttpServletResponse response,
                                           @RequestParam(value = "key") String key) throws IOException {
        String redirectUrl = String.format("%s/security", this.config.get("myconextBaseUrl"));
        doCreateUserFromInstitutionKey(request, response, key, redirectUrl);
    }

    @GetMapping({"/mobile/api/create-from-mobile-api", "/mobile/api/create-from-mobile-api/in-app"})
    public void createFromMobileApi(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value = "h") String hash) throws IOException {
        String redirectUrl = String.format("%s/client/mobile/created", this.config.get("accountBaseUrl"));
        doCreateUserFromInstitutionKey(request, response, hash, redirectUrl);
    }

    private void doCreateUserFromInstitutionKey(HttpServletRequest request,
                                                HttpServletResponse response,
                                                String key,
                                                String redirectUrl) throws IOException {
        LOG.info(String.format("Create-from-institution-login called with key=%s, fallback redirectUrl=%s", key, redirectUrl));
        User user = userRepository.findUserByCreateFromInstitutionKey(key)
                .orElseThrow(() -> new UserNotFoundException("User by createFromInstitutionKey not found"));
        boolean newUser = user.isNewUser();
        String createFromInstitutionReturnUrl = user.getCreateFromInstitutionReturnUrl();
        LOG.info(String.format("Create-from-institution-login user=%s, newUser=%s, stored return_to=%s, allowed domains=%s",
                user.getEmail(), newUser, createFromInstitutionReturnUrl, createFromInstitutionAllowedReturnDomains));
        user.setNewUser(false);
        user.setCreateFromInstitutionKey(null);
        user.setCreateFromInstitutionReturnUrl(null);
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

        String baseRedirectUrl = CreateFromInstitutionReturnUrlSupport
                .validateAndNormalize(createFromInstitutionReturnUrl, createFromInstitutionAllowedReturnDomains)
                .orElse(redirectUrl);
        String redirectLocation = baseRedirectUrl + String.format(baseRedirectUrl.contains("?") ? "&new=%s" : "?new=%s", newUser ? "true" : "false");

        LOG.info(String.format("User %s create from institutionKey. Redirecting to %s", user.getEmail(), redirectLocation));

        response.sendRedirect(redirectLocation);
    }
}
