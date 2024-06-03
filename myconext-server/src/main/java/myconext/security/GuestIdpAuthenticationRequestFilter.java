package myconext.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import myconext.exceptions.UserNotFoundException;
import myconext.geo.GeoLocation;
import myconext.mail.MailBox;
import myconext.manage.Manage;
import myconext.model.*;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.UserLoginRepository;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.core.xml.schema.XSURI;
import org.opensaml.saml.saml2.core.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import saml.DefaultSAMLService;
import saml.model.SAMLAttribute;
import saml.model.SAMLConfiguration;
import saml.model.SAMLStatus;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static myconext.crypto.HashGenerator.hash;
import static myconext.log.MDCContext.logLoginWithContext;
import static myconext.log.MDCContext.logWithContext;
import static myconext.security.CookieResolver.cookieByName;

@SuppressWarnings("unchecked")
@NoArgsConstructor(force = true)
public class GuestIdpAuthenticationRequestFilter extends OncePerRequestFilter {

    public static final String GUEST_IDP_REMEMBER_ME_COOKIE_NAME = "guest-idp-remember-me";
    public static final String TRACKING_DEVICE_COOKIE_NAME = "TRACKING_DEVICE";
    public static final String BROWSER_SESSION_COOKIE_NAME = "BROWSER_SESSION";
    public static final String REGISTER_MODUS_COOKIE_NAME = "REGISTER_MODUS";
    public static final String TIQR_COOKIE_NAME = "TIQR_COOKIE";
    public static final String REMEMBER_ME_QUESTION_ASKED_COOKIE_NAME = "REMEMBER_ME_QUESTION_ASKED_COOKIE";

    private static final Log LOG = LogFactory.getLog(GuestIdpAuthenticationRequestFilter.class);
    public static final String ROLE_MFA = "ROLE_MFA";

    private final AntPathRequestMatcher ssoSamlRequestMatcher;
    private final AntPathRequestMatcher magicSamlRequestMatcher;
    private final AntPathRequestMatcher continueAfterLoginSamlRequestMatcher;
    private final AntPathRequestMatcher metaDataSamlRequestMatcher;
    private final String redirectUrl;
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final IdentityProviderMetaData identityProviderMetaData;
    @Setter
    private UserRepository userRepository;
    private final UserLoginRepository userLoginRepository;
    private final List<String> accountLinkingContextClassReferences;
    private final GeoLocation geoLocation;

    private final int rememberMeMaxAge;
    private final boolean secureCookie;
    private final String magicLinkUrl;
    private final MailBox mailBox;
    @Setter
    @Getter
    private Manage manage;
    private final ExecutorService executor;
    private final int nudgeAppDays;
    private final int rememberMeQuestionAskedDays;
    private final long expiryNonValidatedDurationDays;
    private final long ssoMFADurationSeconds;
    private final String mobileAppROEntityId;
    private final boolean featureDefaultRememberMe;
    private final DefaultSAMLService samlService;
    private final CookieValueEncoder cookieValueEncoder;

    public GuestIdpAuthenticationRequestFilter(String redirectUrl,
                                               Manage manage,
                                               AuthenticationRequestRepository authenticationRequestRepository,
                                               UserRepository userRepository,
                                               UserLoginRepository userLoginRepository,
                                               GeoLocation geoLocation,
                                               int rememberMeMaxAge,
                                               int nudgeAppDays,
                                               int rememberMeQuestionAskedDays,
                                               boolean secureCookie,
                                               String magicLinkUrl,
                                               MailBox mailBox,
                                               long expiryNonValidatedDurationDays,
                                               long ssoMFADurationSeconds,
                                               String mobileAppROEntityId,
                                               boolean featureDefaultRememberMe,
                                               SAMLConfiguration configuration,
                                               IdentityProviderMetaData identityProviderMetaData,
                                               CookieValueEncoder cookieValueEncoder) {
        this.cookieValueEncoder = cookieValueEncoder;
        this.ssoSamlRequestMatcher = new AntPathRequestMatcher("/saml/guest-idp/SSO/**");
        this.magicSamlRequestMatcher = new AntPathRequestMatcher("/saml/guest-idp/magic/**");
        this.continueAfterLoginSamlRequestMatcher = new AntPathRequestMatcher("/saml/guest-idp/continue/**");
        this.metaDataSamlRequestMatcher = new AntPathRequestMatcher("/saml/guest-idp/metadata/**");
        this.redirectUrl = redirectUrl;
        this.manage = manage;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.userLoginRepository = userLoginRepository;
        this.geoLocation = geoLocation;
        this.accountLinkingContextClassReferences = ACR.allAccountLinkingContextClassReferences();
        this.rememberMeMaxAge = rememberMeMaxAge;
        this.nudgeAppDays = nudgeAppDays;
        this.rememberMeQuestionAskedDays = rememberMeQuestionAskedDays;
        this.secureCookie = secureCookie;
        this.magicLinkUrl = magicLinkUrl;
        this.mailBox = mailBox;
        this.expiryNonValidatedDurationDays = expiryNonValidatedDurationDays;
        this.ssoMFADurationSeconds = ssoMFADurationSeconds;
        this.mobileAppROEntityId = mobileAppROEntityId;
        this.featureDefaultRememberMe = featureDefaultRememberMe;
        this.samlService = new DefaultSAMLService(configuration);
        this.executor = Executors.newSingleThreadExecutor();
        this.identityProviderMetaData = identityProviderMetaData;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (this.ssoSamlRequestMatcher.matches(request)) {
            LOG.debug("Starting SSO filter");
            this.sso(request, response);
            return;
        } else if (this.magicSamlRequestMatcher.matches(request)) {
            LOG.debug("Starting magic filter");
            this.magic(request, response);
            return;
        } else if (this.continueAfterLoginSamlRequestMatcher.matches(request)) {
            LOG.debug("Starting continue after login filter");
            this.continueAfterLogin(request, response);
            return;
        } else if (this.metaDataSamlRequestMatcher.matches(request)) {
            LOG.debug("Starting metadata filter");
            this.metaData(response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void sso(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String samlRequest = request.getParameter("SAMLRequest");
        String relayState = request.getParameter("RelayState");

        if (!StringUtils.hasText(samlRequest)) {
            //prevent null-pointer and drop dead
            return;
        }
        if (!HttpMethod.GET.name().equalsIgnoreCase(request.getMethod())) {
            throw new IllegalArgumentException("Only GET redirect are support. Not: " + request.getMethod());
        }
        AuthnRequest authnRequest = this.samlService.parseAuthnRequest(samlRequest, true, true);

        String requesterEntityId = requesterId(authnRequest);
        String issuer = authnRequest.getIssuer().getValue();

        List<String> authenticationContextClassReferenceValues = getAuthenticationContextClassReferenceValues(authnRequest);
        boolean accountLinkingRequired =
                this.accountLinkingContextClassReferences.stream().anyMatch(authenticationContextClassReferenceValues::contains);
        boolean mfaProfileRequired = authenticationContextClassReferenceValues.contains(ACR.PROFILE_MFA);

        SamlAuthenticationRequest samlAuthenticationRequest = new SamlAuthenticationRequest(
                authnRequest.getID(),
                issuer,
                authnRequest.getAssertionConsumerServiceURL(),
                relayState,
                StringUtils.hasText(requesterEntityId) ? requesterEntityId : "",
                accountLinkingRequired,
                mfaProfileRequired,
                authenticationContextClassReferenceValues
        );

        Optional<Cookie> rememberMeCookieOptional = cookieByName(request, GUEST_IDP_REMEMBER_ME_COOKIE_NAME);
        Optional<User> userRememberedOptional = rememberMeCookieOptional.map(this::userFromCookie).flatMap(identity());

        Optional<User> userFromAuthentication = userFromAuthentication();

        User previousAuthenticatedUser = userRememberedOptional.orElse(userFromAuthentication.orElse(null));

        String serviceName = this.manage.getServiceName(request, samlAuthenticationRequest);

        // Use the returned instance for further operations as the save operation has added the _id
        samlAuthenticationRequest.setServiceName(serviceName);
        samlAuthenticationRequest = authenticationRequestRepository.save(samlAuthenticationRequest);

        if (previousAuthenticatedUser != null && !authnRequest.isForceAuthn()) {
            boolean applySsoMfa = isApplySsoMfa();
            if ((!applySsoMfa && mfaProfileRequired) || (accountLinkingRequired && !isUserVerifiedByInstitution(previousAuthenticatedUser,
                    authenticationContextClassReferenceValues))) {
                boolean hasStudentAffiliation = hasRequiredStudentAffiliation((previousAuthenticatedUser.allEduPersonAffiliations()));
                String explanation = ACR.explanationKeyWord(authenticationContextClassReferenceValues, hasStudentAffiliation);
                samlAuthenticationRequest.setUserId(previousAuthenticatedUser.getId());
                samlAuthenticationRequest.setHash(hash());
                authenticationRequestRepository.save(samlAuthenticationRequest);
                addBrowserIdentificationCookie(response);
                String redirect = "/stepup/";
                String mfa = "";
                if (mfaProfileRequired) {
                    List<String> loginOptions = previousAuthenticatedUser.loginOptions();
                    if (loginOptions.contains(LoginOptions.APP.getValue())) {
                        redirect = "/" + LoginOptions.APP.getValue().toLowerCase() + "/";
                        mfa = "&mfa=true";
                    } else {
                        redirect = "/" + loginOptions.get(0).toLowerCase() + "/";
                    }
                }
                String location = this.redirectUrl + redirect + samlAuthenticationRequest.getId()
                        + "?explanation=" + explanation + mfa;
                response.sendRedirect(location);
            } else {
                sendAssertion(request, response, samlAuthenticationRequest, previousAuthenticatedUser);
            }
        } else {
            addBrowserIdentificationCookie(response);
            Optional<Cookie> optionalCookie = cookieByName(request, REGISTER_MODUS_COOKIE_NAME);

            LOG.debug("Cookie REGISTER_MODUS_COOKIE_NAME is: " + optionalCookie.map(Cookie::getValue).orElse("Null"));

            String stepUp = accountLinkingRequired ? "&stepup=true" : "";
            String mfa = mfaProfileRequired ? "&mfa=true" : "";
            String preferMagicLink = this.nudgeMagicLink(authnRequest) ? "&magicLink=true" : "";
            String separator = (accountLinkingRequired || mfaProfileRequired || StringUtils.hasText(preferMagicLink)) ? "?n=1" : "";
            String path = optionalCookie.map(c -> "/request/").orElse("/login/");
            String location = this.redirectUrl + path + samlAuthenticationRequest.getId() +
                    separator + stepUp + mfa + preferMagicLink;
            response.sendRedirect(location);
        }
    }

    private boolean isApplySsoMfa() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UserAuthenticationToken) {
            UserAuthenticationToken userAuthenticationToken = (UserAuthenticationToken) authentication;
            long createdAt = userAuthenticationToken.getCreatedAt();
            boolean mfaRole = userAuthenticationToken.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals(ROLE_MFA));
            return mfaRole && (System.currentTimeMillis() - createdAt) < (ssoMFADurationSeconds * 1000);
        }
        return false;
    }

    public boolean isUserVerifiedByInstitution(User user, List<String> authenticationContextClassReferenceValues) {
        authenticationContextClassReferenceValues = authenticationContextClassReferenceValues == null ?
                Collections.emptyList() : authenticationContextClassReferenceValues;
        List<LinkedAccount> linkedAccounts = user.getLinkedAccounts();
        List<ExternalLinkedAccount> externalLinkedAccounts = user.getExternalLinkedAccounts();
        if (CollectionUtils.isEmpty(linkedAccounts) && CollectionUtils.isEmpty(externalLinkedAccounts)) {
            return false;
        }
        Instant now = new Date().toInstant();
        if (authenticationContextClassReferenceValues.contains(ACR.VALIDATE_NAMES_EXTERNAL)) {
            //We don't support multiple ACR's being enforced all, we pick the most rigid one
            return externalLinkedAccounts.stream()
                    .anyMatch(externalLinkedAccount -> externalLinkedAccount.getExpiresAt().toInstant().isBefore(now));
        }
        boolean validatedName = hasValidatedName(user);
        boolean validatedNameACR = authenticationContextClassReferenceValues.contains(ACR.VALIDATE_NAMES);
        List<LinkedAccount> nonExpiredLinkedAccounts = linkedAccounts.stream()
                .filter(linkedAccount -> {
                    Instant expiresAt = linkedAccount.getExpiresAt().toInstant();
                    //Non-validated name ARC's expire much sooner than ACR.VALIDATE_NAMES
                    if (!validatedNameACR) {
                        expiresAt = linkedAccount.getCreatedAt().toInstant().plus(expiryNonValidatedDurationDays, ChronoUnit.DAYS);
                    }
                    return now.isBefore(expiresAt);
                }).collect(toList());
        boolean atLeastOneNotExpired = !CollectionUtils.isEmpty(nonExpiredLinkedAccounts);
        boolean hasRequiredStudentAffiliation = !authenticationContextClassReferenceValues.contains(ACR.AFFILIATION_STUDENT) ||
                nonExpiredLinkedAccounts.stream()
                        .anyMatch(linkedAccount -> hasRequiredStudentAffiliation(linkedAccount.getEduPersonAffiliations()));
        boolean hasValidatedNames = !authenticationContextClassReferenceValues.contains(ACR.VALIDATE_NAMES) ||
                validatedName;
        return atLeastOneNotExpired && hasRequiredStudentAffiliation && hasValidatedNames;
    }

    public static boolean hasValidatedName(User user) {
        return user.getLinkedAccounts().stream().anyMatch(LinkedAccount::areNamesValidated) ||
                user.getExternalLinkedAccounts().stream().anyMatch(ExternalLinkedAccount::areNamesValidated);
    }

    public static boolean hasValidatedExternalName(User user) {
        return user.getExternalLinkedAccounts().stream().anyMatch(ExternalLinkedAccount::areNamesValidated);
    }


    public static boolean hasRequiredStudentAffiliation(List<String> affiliations) {
        return !CollectionUtils.isEmpty(affiliations) && affiliations.stream()
                .anyMatch(affiliation -> affiliation.toLowerCase().contains("student"));
    }

    private List<String> getAuthenticationContextClassReferenceValues(AuthnRequest authenticationRequest) {
        RequestedAuthnContext requestedAuthnContext = authenticationRequest.getRequestedAuthnContext();
        if (requestedAuthnContext == null) {
            return Collections.emptyList();
        }
        List<AuthnContextClassRef> authnContextClassRefs = requestedAuthnContext.getAuthnContextClassRefs();
        if (authnContextClassRefs == null) {
            return Collections.emptyList();
        }
        return authnContextClassRefs.stream()
                .map(XSURI::getURI)
                .collect(toList());
    }

    private Optional<User> userFromCookie(Cookie remembered) {
        LOG.debug("Returning user from rememberMe cookie");
        return authenticationRequestRepository.findByRememberMeValue(remembered.getValue())
                .map(req -> userRepository.findById(req.getUserId())).flatMap(identity());
    }

    private Optional<User> userFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        LOG.debug("Attempting user authentication from security context: " + authentication);
        if (authentication != null && authentication.isAuthenticated() && authentication instanceof UsernamePasswordAuthenticationToken) {
            //The user from the mongodb session possible contains the "@" dot replacement - need to refetch to prevent this
            User user = (User) authentication.getPrincipal();
            return userRepository.findById(user.getId());
        }
        return Optional.empty();
    }

    private void addBrowserIdentificationCookie(HttpServletResponse response) {
        response.setHeader("Set-Cookie", BROWSER_SESSION_COOKIE_NAME + "=true; SameSite=Lax" + (secureCookie ? "; Secure" : ""));
    }

    private String requesterId(AuthnRequest authenticationRequest) {
        Issuer issuer = authenticationRequest.getIssuer();
        String issuerValue = issuer != null ? issuer.getValue() : "";
        Scoping scoping = authenticationRequest.getScoping();
        List<RequesterID> requesterIDS = scoping != null ? scoping.getRequesterIDs() : null;
        return CollectionUtils.isEmpty(requesterIDS) ? issuerValue : requesterIDS.get(0).getURI();
    }

    private boolean nudgeMagicLink(AuthnRequest authenticationRequest) {
        Scoping scoping = authenticationRequest.getScoping();
        List<RequesterID> requesterIDS = scoping != null ? scoping.getRequesterIDs() : null;
        return !CollectionUtils.isEmpty(requesterIDS) && requesterIDS.stream()
                .anyMatch(requesterID -> this.mobileAppROEntityId.equalsIgnoreCase(requesterID.getURI()));
    }

    private void magic(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String hash = request.getParameter("h");
        if (!StringUtils.hasText(hash)) {
            //Log all headers to verify / confirm that this is a prefetch
            Map<String, String> headerMap = Collections.list(request.getHeaderNames()).stream()
                    .collect(toMap(headerName -> headerName, request::getHeader));
            LOG.warn("Magic endpoint without 'h' parameter. Headers: " + headerMap);

            response.sendRedirect(this.redirectUrl + "/expired");
            return;
        }
        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findByHash(hash);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            response.sendRedirect(this.redirectUrl + "/expired");
            return;
        }
        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        List<String> authenticationContextClassReferences = samlAuthenticationRequest.getAuthenticationContextClassReferences();
        boolean accountLinkingRequired = samlAuthenticationRequest.isAccountLinkingRequired() &&
                !this.isUserVerifiedByInstitution(user, authenticationContextClassReferences);

        String charSet = Charset.defaultCharset().name();

        boolean hasStudentAffiliation = hasRequiredStudentAffiliation(user.allEduPersonAffiliations());
        String explanation = ACR.explanationKeyWord(authenticationContextClassReferences, hasStudentAffiliation);

        if (accountLinkingRequired && StepUpStatus.NONE.equals(samlAuthenticationRequest.getSteppedUp())) {
            response.sendRedirect(this.redirectUrl + "/stepup/" + samlAuthenticationRequest.getId()
                    + "?explanation=" + explanation);
            return;
        }
        String force = request.getParameter("force");
        if (samlAuthenticationRequest.isMfaProfileRequired() && !samlAuthenticationRequest.isTiqrFlow() && !StringUtils.hasText(force)) {
            response.sendRedirect(this.redirectUrl + "/app-required?h=" + hash +
                    "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet));
            return;
        }

        Optional<Cookie> optionalCookie = cookieByName(request, BROWSER_SESSION_COOKIE_NAME);
        if (!optionalCookie.isPresent()) {
            samlAuthenticationRequest.setLoginStatus(LoginStatus.LOGGED_IN_DIFFERENT_DEVICE);
            samlAuthenticationRequest.setVerificationCode(VerificationCodeGenerator.generate());
            if (incrementVerificationCodeRetry(samlAuthenticationRequest)) {
                response.sendRedirect(this.redirectUrl + "/max-attempts");
                return;
            }
            mailBox.sendVerificationCode(user, samlAuthenticationRequest.getVerificationCode());
            response.sendRedirect(this.redirectUrl + "/success");
            return;
        }

        boolean proceed = checkStepUp(response, request, hash, samlAuthenticationRequest, user, charSet, explanation);
        if (!proceed) {
            return;
        }

        samlAuthenticationRequest.setLoginStatus(LoginStatus.LOGGED_IN_SAME_DEVICE);
        doSendAssertion(request, response, samlAuthenticationRequest, user);
    }

    private boolean incrementVerificationCodeRetry(SamlAuthenticationRequest samlAuthenticationRequest) {
        int retryVerificationCode = samlAuthenticationRequest.getRetryVerificationCode();
        samlAuthenticationRequest.setRetryVerificationCode(retryVerificationCode + 1);
        authenticationRequestRepository.save(samlAuthenticationRequest);
        return retryVerificationCode > 2;
    }

    private boolean checkStepUp(HttpServletResponse response, HttpServletRequest request, String hash,
                                SamlAuthenticationRequest samlAuthenticationRequest, User user, String charSet,
                                String explanation) throws IOException {
        boolean inStepUpFlow = StepUpStatus.IN_STEP_UP.equals(samlAuthenticationRequest.getSteppedUp());

        boolean hasStudentAffiliation = hasRequiredStudentAffiliation(user.allEduPersonAffiliations());
        List<String> authenticationContextClassReferences = samlAuthenticationRequest.getAuthenticationContextClassReferences();
        boolean missingStudentAffiliation = !CollectionUtils.isEmpty(authenticationContextClassReferences) && authenticationContextClassReferences.contains(ACR.AFFILIATION_STUDENT) &&
                !hasStudentAffiliation;
        boolean missingValidName = !CollectionUtils.isEmpty(authenticationContextClassReferences) && authenticationContextClassReferences.contains(ACR.VALIDATE_NAMES) &&
                !hasValidatedName(user);

        if (user.isNewUser()) {
            user.setNewUser(false);
            user.setLastSeenAppNudge(System.currentTimeMillis());
            userRepository.save(user);

            logWithContext(user, "add", "account", LOG, "Saving user after new registration and magic link");
            mailBox.sendAccountConfirmation(user);
            if (inStepUpFlow) {
                finishStepUp(samlAuthenticationRequest);
            }
            if (missingStudentAffiliation || missingValidName) {
                //When we send the assertion EB stops the flow but this will be fixed upstream
                return true;
            }
            if (samlAuthenticationRequest.isTiqrFlow()) {
                return true;
            }
            String url = this.redirectUrl + "/confirm?h=" + hash +
                    "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet) +
                    "&email=" + URLEncoder.encode(user.getEmail(), charSet) +
                    "&new=true";
            if (!StepUpStatus.NONE.equals(samlAuthenticationRequest.getSteppedUp())) {
                url += "&explanation=" + explanation;
            }
            response.sendRedirect(url);
            return false;
        } else if (inStepUpFlow) {
            finishStepUp(samlAuthenticationRequest);
            if (missingStudentAffiliation || missingValidName) {
                //When we send the assertion EB stops the flow but this will be fixed upstream
                return true;
            }
            boolean externalNameValidation = !CollectionUtils.isEmpty(authenticationContextClassReferences)
                    && authenticationContextClassReferences.contains(ACR.VALIDATE_NAMES_EXTERNAL);
            String path = externalNameValidation ? "confirm-external-stepup" : "confirm-stepup";
            response.sendRedirect(this.redirectUrl + "/" + path + "?h=" + hash +
                    "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet) +
                    "&explanation=" + explanation +
                    "&ref=" + user.getId());
            return false;
        } else if (!samlAuthenticationRequest.isPasswordOrWebAuthnFlow() && !samlAuthenticationRequest.isTiqrFlow() &&
                !user.loginOptions().contains(LoginOptions.APP.getValue()) &&
                user.getLastSeenAppNudge() < (System.currentTimeMillis() - 1000L * 60 * 60 * 24 * nudgeAppDays)) {
            //Nudge user to use the app
            user.setLastSeenAppNudge(System.currentTimeMillis());
            userRepository.save(user);

            String url = this.redirectUrl + "/confirm?h=" + hash +
                    "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet) +
                    "&new=false";
            response.sendRedirect(url);
            return false;
        } else if (!this.featureDefaultRememberMe && !samlAuthenticationRequest.isRememberMeQuestionAsked()) {
            samlAuthenticationRequest.setRememberMeQuestionAsked(true);
            authenticationRequestRepository.save(samlAuthenticationRequest);
            if (cookieByName(request, REMEMBER_ME_QUESTION_ASKED_COOKIE_NAME).isPresent()) {
                return true;
            }
            addRememberMeQuestionAskedCookie(response);
            String url = this.redirectUrl + "/remember?h=" + hash +
                    "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet);
            response.sendRedirect(url);
            return false;
        }
        return true;
    }

    private void continueAfterLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = request.getParameter("id");
        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findById(id);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            response.sendRedirect(this.redirectUrl + "/expired");
            return;
        }
        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();
        String verificationCode = request.getParameter("verificationCode");
        if (!StringUtils.hasText(verificationCode) || !verificationCode.equals(samlAuthenticationRequest.getVerificationCode())) {
            if (incrementVerificationCodeRetry(samlAuthenticationRequest)) {
                response.sendRedirect(this.redirectUrl + "/max-attempts");
                return;
            }
            String currentUrl = URLDecoder.decode(request.getParameter("currentUrl"), Charset.defaultCharset().name());
            response.sendRedirect(currentUrl + "&mismatch=true");
            return;
        }
        if (samlAuthenticationRequest.getLoginStatus().equals(LoginStatus.NOT_LOGGED_IN)) {
            response.sendRedirect(this.redirectUrl + "/expired");
            return;
        }
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        String charSet = Charset.defaultCharset().name();
        boolean hasStudentAffiliation = hasRequiredStudentAffiliation(user.allEduPersonAffiliations());
        List<String> authenticationContextClassReferences = samlAuthenticationRequest.getAuthenticationContextClassReferences();
        String explanation = ACR.explanationKeyWord(authenticationContextClassReferences, hasStudentAffiliation);

        boolean proceed = this.checkStepUp(response, request, samlAuthenticationRequest.getHash(), samlAuthenticationRequest, user, charSet, explanation);
        if (!proceed) {
            return;
        }
        doSendAssertion(request, response, samlAuthenticationRequest, user);
    }

    private void doSendAssertion(HttpServletRequest request,
                                 HttpServletResponse response,
                                 SamlAuthenticationRequest samlAuthenticationRequest,
                                 User user) {
        //ensure the magic link can't be used twice
        samlAuthenticationRequest.setHash(null);

        LOG.debug(String.format("Disabling SAML authentication request after login by %s ", user.getUsername()));

        authenticationRequestRepository.save(samlAuthenticationRequest);

        //We support SSO for MFA, we must mark the authentication with a timestamp and an extra role
        boolean mfaProfileRequired = samlAuthenticationRequest.isMfaProfileRequired();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        if (mfaProfileRequired && samlAuthenticationRequest.isTiqrFlow()) {
            authorities = List.of(new SimpleGrantedAuthority(ROLE_MFA), authorities.iterator().next());
        }
        UserAuthenticationToken authentication = new UserAuthenticationToken(user, null,
                authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (this.featureDefaultRememberMe) {
            LOG.info(String.format("Remember me functionality activated for %s ", user.getUsername()));
            addRememberMeCookie(response, samlAuthenticationRequest);
        }
        if (samlAuthenticationRequest.isTiqrFlow()) {
            LOG.info(String.format("Tiqr flow authenticated for %s ", user.getUsername()));
            addTiqrCookie(response, user);
        }
        String loginMethod = samlAuthenticationRequest.isTiqrFlow() ? "tiqr" : "magiclink";

        logLoginWithContext(user, loginMethod, true, LOG, "Successfully logged in with " + loginMethod);

        sendAssertion(request, response, samlAuthenticationRequest, user);
    }

    private void finishStepUp(SamlAuthenticationRequest samlAuthenticationRequest) {
        samlAuthenticationRequest.setSteppedUp(StepUpStatus.FINISHED_STEP_UP);
        authenticationRequestRepository.save(samlAuthenticationRequest);
    }

    private void addRememberMeCookie(HttpServletResponse response, SamlAuthenticationRequest samlAuthenticationRequest) {
        Cookie cookie = new Cookie(GUEST_IDP_REMEMBER_ME_COOKIE_NAME, samlAuthenticationRequest.getRememberMeValue());
        cookie.setMaxAge(rememberMeMaxAge);
        cookie.setSecure(secureCookie);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }

    private void addTiqrCookie(HttpServletResponse response, User user) {
        Cookie cookie = new Cookie(TIQR_COOKIE_NAME, this.cookieValueEncoder.encode(user.getUsername()));
        cookie.setMaxAge(rememberMeMaxAge);
        cookie.setSecure(secureCookie);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void addRememberMeQuestionAskedCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REMEMBER_ME_QUESTION_ASKED_COOKIE_NAME, Boolean.TRUE.toString());
        cookie.setMaxAge(rememberMeQuestionAskedDays * 60 * 60 * 24);
        cookie.setSecure(secureCookie);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void addTrackingCookie(HttpServletRequest request, HttpServletResponse response, User user) {
        Optional<Cookie> optionalCookie = cookieByName(request, TRACKING_DEVICE_COOKIE_NAME);
        boolean existingUser = StringUtils.hasText(user.getTrackingUuid());
        if (!existingUser) {
            user.setTrackingUuid(UUID.randomUUID().toString());
            userRepository.save(user);
        }
        if (!optionalCookie.isPresent() || !user.getTrackingUuid().equalsIgnoreCase(optionalCookie.get().getValue())) {
            Cookie cookie = new Cookie(TRACKING_DEVICE_COOKIE_NAME, user.getTrackingUuid());
            cookie.setMaxAge(Integer.MAX_VALUE - 1);
            cookie.setSecure(secureCookie);
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            //can not pass HttpServletRequest to different thread
            Map<String, String> headers = Collections.list(request.getHeaderNames()).stream().collect(Collectors.toMap(s -> s, request::getHeader));
            headers.put("ipAddress", request.getRemoteAddr());
            //avoid delay due to InetAddress lookup
            executor.submit(() -> {
                UserLogin userLogin = new UserLogin(user, headers);
                String lookupAddress = userLogin.getLookupAddress();
                Optional<String> optionalLocation = geoLocation.findLocation(lookupAddress);
                optionalLocation.ifPresent(location -> userLogin.setIpLocation(location));
                userLoginRepository.save(userLogin);
                if (existingUser) {
                    mailBox.sendNewDevice(user, userLogin);
                }
            });
        }
    }

    private void sendAssertion(HttpServletRequest request,
                               HttpServletResponse response,
                               SamlAuthenticationRequest samlAuthenticationRequest,
                               User user) {
        String relayState = samlAuthenticationRequest.getRelayState();
        String requesterEntityId = samlAuthenticationRequest.getRequesterEntityId();

        List<String> authenticationContextClassReferences = samlAuthenticationRequest.getAuthenticationContextClassReferences();
        List<SAMLAttribute> attributes = attributes(user, requesterEntityId);

        boolean applySsoMfa = this.isApplySsoMfa();
        SAMLStatus samlStatus = SAMLStatus.SUCCESS;
        String optionalMessage = null;
        String authnContextClassRefValue = DefaultSAMLService.authnContextClassRefPassword;
        if (samlAuthenticationRequest.isAccountLinkingRequired()) {
            boolean hasStudentAffiliation = hasRequiredStudentAffiliation(user.allEduPersonAffiliations());

            //When we change the status of the response to NO_AUTH_CONTEXT EB stops the flow but this will be fixed upstream
            boolean missingStudentAffiliation = authenticationContextClassReferences.contains(ACR.AFFILIATION_STUDENT) &&
                    !hasStudentAffiliation;
            boolean missingValidName = authenticationContextClassReferences.contains(ACR.VALIDATE_NAMES) &&
                    !hasValidatedName(user);
            boolean missingExternalName = authenticationContextClassReferences.contains(ACR.VALIDATE_NAMES_EXTERNAL) &&
                    !hasValidatedExternalName(user);
            if (missingStudentAffiliation || missingValidName || missingExternalName) {
                if (missingValidName) {
                    optionalMessage = "The requesting service has indicated that the authenticated user is required to have a first_name and last_name." +
                            " Your institution has not provided those attributes.";
                } else if (missingExternalName) {
                    optionalMessage = "The requesting service has indicated that the authenticated user has verified their first_name and last_name." +
                            " Your identity is not verified by an external trusted party.";
                } else {
                    optionalMessage = "The requesting service has indicated that the authenticated user is required to have an affiliation Student." +
                            " Your institution has not provided this affiliation.";
                }
                samlStatus = SAMLStatus.NO_AUTHN_CONTEXT;
            } else {
                authnContextClassRefValue = ACR.selectACR(authenticationContextClassReferences, hasStudentAffiliation);
            }
        } else if (samlAuthenticationRequest.isMfaProfileRequired()) {
            if (samlAuthenticationRequest.isTiqrFlow() || applySsoMfa) {
                authnContextClassRefValue = ACR.selectACR(authenticationContextClassReferences, false);
            } else {
                optionalMessage = "The requesting service has indicated that a login with the eduID app is required to login.";
                samlStatus = SAMLStatus.NO_AUTHN_CONTEXT;
            }
        } else if (!applySsoMfa && !CollectionUtils.isEmpty(authenticationContextClassReferences)) {
            optionalMessage = String.format("The specified authentication context requirements '%s' cannot be met by the responder.",
                    String.join(", ", authenticationContextClassReferences));
            samlStatus = SAMLStatus.NO_AUTHN_CONTEXT;
        }
        if (!samlStatus.equals(SAMLStatus.SUCCESS)) {
            authnContextClassRefValue = DefaultSAMLService.authnContextClassRefUnspecified;
        }
        Optional<Cookie> optionalCookie = cookieByName(request, BROWSER_SESSION_COOKIE_NAME);
        optionalCookie.ifPresent(cookie -> {
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        });
        //Tracking cookie for user new device discovery
        this.addTrackingCookie(request, response, user);
        this.samlService.sendResponse(
                samlAuthenticationRequest.getIssuer(),
                samlAuthenticationRequest.getRequestId(),
                user.getUid(),
                samlStatus,
                relayState,
                optionalMessage,
                authnContextClassRefValue,
                attributes,
                response
        );
    }

    protected List<SAMLAttribute> attributes(User user, String requesterEntityId) {
        List<LinkedAccount> linkedAccounts = safeSortedAffiliations(user);
        String givenName = user.getGivenName();
        String familyName = user.getFamilyName();
        String chosenName = user.getChosenName();
        if (!StringUtils.hasText(chosenName)) {
            chosenName = givenName;
            //Migrate at JIT as only users with linked-accounts were migrated in myconext.mongo.Migrations#migrateUsers
            user.setChosenName(chosenName);
        }
        //Corner-case, but can happen
        if (!StringUtils.hasText(givenName)) {
            givenName = chosenName;
        }
        String displayName = String.format("%s %s", chosenName, familyName);
        String eppn = user.getEduPersonPrincipalName();
        List<SAMLAttribute> attributes = new ArrayList(Arrays.asList(
                attribute("urn:mace:dir:attribute-def:cn", displayName),
                attribute("urn:mace:dir:attribute-def:displayName", displayName),
                attribute("urn:mace:dir:attribute-def:eduPersonPrincipalName", eppn),
                attribute("urn:oasis:names:tc:SAML:attribute:subject-id", eppn),
                attribute("urn:mace:dir:attribute-def:givenName", givenName),
                attribute("urn:mace:dir:attribute-def:mail", user.getEmail()),
                attribute("urn:mace:dir:attribute-def:sn", familyName),
                attribute("urn:mace:dir:attribute-def:uid", user.getUid()),
                attribute("urn:mace:terena.org:attribute-def:schacHomeOrganization", user.getSchacHomeOrganization())
        ));
        String eduIDValue = user.computeEduIdForServiceProviderIfAbsent(requesterEntityId, manage);
        userRepository.save(user);

        attributes.add(attribute("urn:mace:eduid.nl:1.1", eduIDValue));
        if (user.getDateOfBirth() != null) {
            // https://wiki.refeds.org/display/STAN/SCHAC+Releases
            String dateOfBirth = DateTimeFormatter
                    .ofPattern("yyyyMMdd")
                    .withZone(ZoneId.systemDefault())
                    .format(user.getDateOfBirth().toInstant());
            attributes.add(attribute("urn:schac:attribute-def:schacDateOfBirth", dateOfBirth));
        }

        user.getAttributes()
                .forEach((key, values) ->
                        values.forEach(value -> attributes.add(attribute(key, value))));


        List<String> scopedAffiliations = linkedAccounts.stream()
                .map(linkedAccount -> linkedAccount.getEduPersonAffiliations().stream()
                        .map(affiliation -> affiliation.contains("@")
                                ? affiliation : String.format("%s@%s", affiliation, linkedAccount.getSchacHomeOrganization()))
                        .collect(toList()))
                .flatMap(Collection::stream).distinct().collect(Collectors.toList());
        scopedAffiliations.add("affiliate@eduid.nl");
        scopedAffiliations.forEach(aff -> attributes.add(attribute("urn:mace:dir:attribute-def:eduPersonScopedAffiliation", aff)));

        List<String> affiliations = scopedAffiliations.stream().map(affiliation -> affiliation.substring(0, affiliation.indexOf("@")))
                .distinct().collect(toList());
        affiliations.forEach(aff -> attributes.add(attribute("urn:mace:dir:attribute-def:eduPersonAffiliation", aff)));
        return attributes;
    }

    private List<LinkedAccount> safeSortedAffiliations(User user) {
        List<LinkedAccount> linkedAccounts = user.linkedAccountsSorted();
        List<LinkedAccount> linkedAccountsEmptyAffiliations = linkedAccounts.stream()
                .filter(linkedAccount -> CollectionUtils.isEmpty(linkedAccount.getEduPersonAffiliations()))
                .collect(toList());
        linkedAccountsEmptyAffiliations.forEach(linkedAccount -> linkedAccount.setEduPersonAffiliations(
                Collections.singletonList("affiliation@" + linkedAccount.getSchacHomeOrganization())));
        if (!linkedAccountsEmptyAffiliations.isEmpty()) {
            userRepository.save(user);
        }
        return user.linkedAccountsSorted();
    }

    private SAMLAttribute attribute(String name, String value) {
        return new SAMLAttribute(name, value);
    }

    private void metaData(HttpServletResponse servletResponse) throws IOException {
        servletResponse.setContentType("text/xml");
        servletResponse.setCharacterEncoding(UTF_8.name());

        servletResponse.setHeader("Cache-Control", "private");
        String metaData = this.samlService.metaData(
                this.identityProviderMetaData.getSingleSignOnServiceURI(),
                this.identityProviderMetaData.getName(),
                this.identityProviderMetaData.getDescription(),
                this.identityProviderMetaData.getLogoURI()
        );
        servletResponse.getWriter().write(metaData);
    }


}
