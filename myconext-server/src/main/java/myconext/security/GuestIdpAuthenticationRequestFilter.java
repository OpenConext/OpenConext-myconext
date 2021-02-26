package myconext.security;

import myconext.exceptions.ForbiddenException;
import myconext.exceptions.UserNotFoundException;
import myconext.mail.MailBox;
import myconext.manage.ServiceProviderResolver;
import myconext.model.*;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SamlMessageStore;
import org.springframework.security.saml.SamlRequestMatcher;
import org.springframework.security.saml.provider.identity.IdentityProviderService;
import org.springframework.security.saml.provider.identity.IdpAuthenticationRequestFilter;
import org.springframework.security.saml.provider.provisioning.SamlProviderProvisioning;
import org.springframework.security.saml.saml2.attribute.Attribute;
import org.springframework.security.saml.saml2.attribute.AttributeNameFormat;
import org.springframework.security.saml.saml2.authentication.*;
import org.springframework.security.saml.saml2.metadata.Binding;
import org.springframework.security.saml.saml2.metadata.Endpoint;
import org.springframework.security.saml.saml2.metadata.NameId;
import org.springframework.security.saml.saml2.metadata.ServiceProviderMetadata;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static myconext.api.UserController.hash;
import static myconext.log.MDCContext.logLoginWithContext;
import static myconext.log.MDCContext.logWithContext;
import static myconext.security.CookieResolver.cookieByName;
import static org.springframework.util.StringUtils.hasText;

@SuppressWarnings("unchecked")
public class GuestIdpAuthenticationRequestFilter extends IdpAuthenticationRequestFilter {

    public static final String GUEST_IDP_REMEMBER_ME_COOKIE_NAME = "guest-idp-remember-me";
    public static final String BROWSER_SESSION_COOKIE_NAME = "BROWSER_SESSION";
    public static final String REGISTER_MODUS_COOKIE_NAME = "REGISTER_MODUS";

    private static final Log LOG = LogFactory.getLog(GuestIdpAuthenticationRequestFilter.class);

    private final SamlRequestMatcher ssoSamlRequestMatcher;
    private final SamlRequestMatcher magicSamlRequestMatcher;
    private final SamlRequestMatcher continueAfterloginSamlRequestMatcher;
    private final String redirectUrl;
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;
    private final List<String> accountLinkingContextClassReferences;

    private final int rememberMeMaxAge;
    private final boolean secureCookie;
    private final String magicLinkUrl;
    private final MailBox mailBox;
    private final ServiceProviderResolver serviceProviderResolver;

    public GuestIdpAuthenticationRequestFilter(SamlProviderProvisioning<IdentityProviderService> provisioning,
                                               SamlMessageStore<Assertion, HttpServletRequest> assertionStore,
                                               String redirectUrl,
                                               ServiceProviderResolver serviceProviderResolver,
                                               AuthenticationRequestRepository authenticationRequestRepository,
                                               UserRepository userRepository,
                                               int rememberMeMaxAge,
                                               boolean secureCookie,
                                               String magicLinkUrl,
                                               MailBox mailBox) {
        super(provisioning, assertionStore);
        this.ssoSamlRequestMatcher = new SamlRequestMatcher(provisioning, "SSO");
        this.magicSamlRequestMatcher = new SamlRequestMatcher(provisioning, "magic");
        this.continueAfterloginSamlRequestMatcher = new SamlRequestMatcher(provisioning, "continue");
        this.redirectUrl = redirectUrl;
        this.serviceProviderResolver = serviceProviderResolver;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.accountLinkingContextClassReferences = ACR.all();
        this.rememberMeMaxAge = rememberMeMaxAge;
        this.secureCookie = secureCookie;
        this.magicLinkUrl = magicLinkUrl;
        this.mailBox = mailBox;
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
        } else if (this.continueAfterloginSamlRequestMatcher.matches(request)) {
            LOG.debug("Starting continue after login filter");
            this.continueAfterLogin(request, response);
            return;
        }
        super.doFilterInternal(request, response, filterChain);
    }

    private void sso(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IdentityProviderService provider = getProvisioning().getHostedProvider();
        String samlRequest = request.getParameter("SAMLRequest");
        String relayState = request.getParameter("RelayState");

        if (StringUtils.isEmpty(samlRequest)) {
            //prevent null-pointer and drop dead
            return;
        }
        AuthenticationRequest authenticationRequest =
                provider.fromXml(samlRequest, true, isDeflated(request), AuthenticationRequest.class);
        provider.validate(authenticationRequest);

        String requesterEntityId = requesterId(authenticationRequest);
        String issuer = authenticationRequest.getIssuer().getValue();

        List<String> authenticationContextClassReferenceValues = getAuthenticationContextClassReferenceValues(authenticationRequest);
        boolean accountLinkingRequired =
                this.accountLinkingContextClassReferences.stream().anyMatch(authenticationContextClassReferenceValues::contains);

        SamlAuthenticationRequest samlAuthenticationRequest = new SamlAuthenticationRequest(
                authenticationRequest.getId(),
                issuer,
                authenticationRequest.getAssertionConsumerService().getLocation(),
                relayState,
                StringUtils.hasText(requesterEntityId) ? requesterEntityId : "",
                accountLinkingRequired,
                authenticationContextClassReferenceValues
        );

        // Use the returned instance for further operations as the save operation has added the _id
        samlAuthenticationRequest = authenticationRequestRepository.save(samlAuthenticationRequest);

        Optional<Cookie> rememberMeCookieOptional = cookieByName(request, GUEST_IDP_REMEMBER_ME_COOKIE_NAME);
        Optional<User> userRememberedOptional = rememberMeCookieOptional.map(this::userFromCookie).flatMap(identity());

        Optional<User> userFromAuthentication = userFromAuthentication();

        User previousAuthenticatedUser = userRememberedOptional.orElse(userFromAuthentication.orElse(null));

        String serviceName = this.getServiceName(request, samlAuthenticationRequest);
        String encodedServiceName = URLEncoder.encode(serviceName, "UTF-8");

        if (previousAuthenticatedUser != null && !authenticationRequest.isForceAuth()) {
            if (accountLinkingRequired && !isUserVerifiedByInstitution(previousAuthenticatedUser,
                    authenticationContextClassReferenceValues)) {
                boolean hasStudentAffiliation = hasRequiredStudentAffiliation((previousAuthenticatedUser.allEduPersonAffiliations()));
                String explanation = ACR.explanationKeyWord(authenticationContextClassReferenceValues, hasStudentAffiliation);
                samlAuthenticationRequest.setUserId(previousAuthenticatedUser.getId());
                samlAuthenticationRequest.setHash(hash());
                authenticationRequestRepository.save(samlAuthenticationRequest);
                addBrowserIdentificationCookie(response);
                response.sendRedirect(this.redirectUrl + "/stepup/" + samlAuthenticationRequest.getId() + "?name=" + encodedServiceName + "&explanation=" + explanation);
            } else {
                ServiceProviderMetadata serviceProviderMetadata = provider.getRemoteProvider(samlAuthenticationRequest.getIssuer());
                sendAssertion(request, response, samlAuthenticationRequest, previousAuthenticatedUser,
                        provider, serviceProviderMetadata, authenticationRequest);
            }
        } else {
            addBrowserIdentificationCookie(response);
            Optional<Cookie> optionalCookie = cookieByName(request, REGISTER_MODUS_COOKIE_NAME);

            LOG.debug("Cookie REGISTER_MODUS_COOKIE_NAME is: " + optionalCookie.map(Cookie::getValue).orElse("Null"));

            String modus = optionalCookie.map(c -> "&modus=cr").orElse("");
            String stepUp = accountLinkingRequired ? "&stepup=true" : "";
            response.sendRedirect(this.redirectUrl + "/login/" + samlAuthenticationRequest.getId() +
                    "?name=" + encodedServiceName + modus + stepUp);
        }
    }

    private String getServiceName(HttpServletRequest request, SamlAuthenticationRequest samlAuthenticationRequest) {
        String lang = cookieByName(request, "lang").map(cookie -> cookie.getValue()).orElse("en");
        Optional<ServiceProvider> optionalServiceProvider = serviceProviderResolver.resolve(samlAuthenticationRequest.getRequesterEntityId());
        String serviceName = optionalServiceProvider.map(serviceProvider -> lang.equals("en") ? serviceProvider.getName() : serviceProvider.getNameNl())
                .orElse(samlAuthenticationRequest.getRequesterEntityId());
        return serviceName;
    }

    public static boolean isUserVerifiedByInstitution(User user, List<String> authenticationContextClassReferenceValues) {
        Date now = new Date();
        List<LinkedAccount> linkedAccounts = user.getLinkedAccounts();
        if (CollectionUtils.isEmpty(linkedAccounts)) {
            return false;
        }
        authenticationContextClassReferenceValues = authenticationContextClassReferenceValues == null ?
                Collections.emptyList() : authenticationContextClassReferenceValues;
        List<LinkedAccount> nonExpiredLinkedAccounts = linkedAccounts.stream()
                .filter(linkedAccount -> now.toInstant().isBefore(linkedAccount.getExpiresAt().toInstant()))
                .collect(toList());
        boolean atLeastOneNotExpired = !CollectionUtils.isEmpty(nonExpiredLinkedAccounts);
        boolean hasRequiredStudentAffiliation = !authenticationContextClassReferenceValues.contains(ACR.AFFILIATION_STUDENT) ||
                nonExpiredLinkedAccounts.stream()
                        .anyMatch(linkedAccount -> hasRequiredStudentAffiliation(linkedAccount.getEduPersonAffiliations()));
        boolean hasValidatedNames = !authenticationContextClassReferenceValues.contains(ACR.VALIDATE_NAMES) ||
                hasValidatedName(user);
        return atLeastOneNotExpired && hasRequiredStudentAffiliation && hasValidatedNames;
    }

    public static boolean hasValidatedName(User user) {
        return hasValidatedName(user.getLinkedAccounts());
    }

    private static boolean hasValidatedName(List<LinkedAccount> linkedAccounts) {
        return linkedAccounts.stream().anyMatch(LinkedAccount::areNamesValidated);
    }

    public static boolean hasRequiredStudentAffiliation(List<String> affiliations) {
        return !CollectionUtils.isEmpty(affiliations) && affiliations.stream()
                .anyMatch(affiliation -> affiliation.startsWith("student"));
    }

    private List<String> getAuthenticationContextClassReferenceValues(AuthenticationRequest authenticationRequest) {
        List<AuthenticationContextClassReference> authenticationContextClassReferences = authenticationRequest.getAuthenticationContextClassReferences();
        if (authenticationContextClassReferences == null) {
            return Collections.emptyList();
        }
        return authenticationContextClassReferences.stream()
                .map(AuthenticationContextClassReference::getValue)
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

    private boolean isDeflated(HttpServletRequest request) {
        return HttpMethod.GET.name().equalsIgnoreCase(request.getMethod());
    }

    private String requesterId(AuthenticationRequest authenticationRequest) {
        Issuer issuer = authenticationRequest.getIssuer();
        String issuerValue = issuer != null ? issuer.getValue() : "";
        Scoping scoping = authenticationRequest.getScoping();
        List<String> requesterIds = scoping != null ? scoping.getRequesterIds() : null;
        return CollectionUtils.isEmpty(requesterIds) ? issuerValue : requesterIds.get(0);
    }

    private void magic(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String hash = request.getParameter("h");
        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findByHash(hash);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            response.sendRedirect(this.redirectUrl + "/expired");
            return;
        }
        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();
        User user = userRepository.findById(samlAuthenticationRequest.getUserId())
                .orElseThrow(UserNotFoundException::new);

        List<String> authenticationContextClassReferences = samlAuthenticationRequest.getAuthenticationContextClassReferences();
        boolean accountLinkingRequired = samlAuthenticationRequest.isAccountLinkingRequired() &&
                !GuestIdpAuthenticationRequestFilter.isUserVerifiedByInstitution(user,
                        authenticationContextClassReferences);

        String charSet = Charset.defaultCharset().name();

        String serviceName = this.getServiceName(request, samlAuthenticationRequest);
        String encodedServiceName = URLEncoder.encode(serviceName, "UTF-8");
        boolean hasStudentAffiliation = hasRequiredStudentAffiliation(user.allEduPersonAffiliations());
        String explanation = ACR.explanationKeyWord(authenticationContextClassReferences, hasStudentAffiliation);

        if (accountLinkingRequired && StepUpStatus.NONE.equals(samlAuthenticationRequest.getSteppedUp())) {
            response.sendRedirect(this.redirectUrl + "/stepup/" + samlAuthenticationRequest.getId()
                    + "?name=" + encodedServiceName + "&explanation=" + explanation);
            return;
        }


        Optional<Cookie> optionalCookie = cookieByName(request, BROWSER_SESSION_COOKIE_NAME);
        if (!optionalCookie.isPresent()) {
            samlAuthenticationRequest.setLoginStatus(LoginStatus.LOGGED_IN_DIFFERENT_DEVICE);
            samlAuthenticationRequest.setVerificationCode(VerificationCodeGenerator.generate());
            int retryVerificationCode = samlAuthenticationRequest.getRetryVerificationCode();
            samlAuthenticationRequest.setRetryVerificationCode(retryVerificationCode + 1);
            authenticationRequestRepository.save(samlAuthenticationRequest);
            if (retryVerificationCode > 2) {
                throw new ForbiddenException();
            }
            mailBox.sendVerificationCode(user, samlAuthenticationRequest.getVerificationCode());
            response.sendRedirect(this.redirectUrl + "/success");
            return;
        }

        boolean proceed = checkStepUp(response, hash, samlAuthenticationRequest, user, charSet, encodedServiceName, explanation);
        if (!proceed) {
            return;
        }

        samlAuthenticationRequest.setLoginStatus(LoginStatus.LOGGED_IN_SAME_DEVICE);
        doSendAssertion(request, response, samlAuthenticationRequest, user);
    }

    private boolean checkStepUp(HttpServletResponse response, String hash, SamlAuthenticationRequest samlAuthenticationRequest, User user, String charSet, String encodedServiceName, String explanation) throws IOException {
        boolean inStepUpFlow = StepUpStatus.IN_STEP_UP.equals(samlAuthenticationRequest.getSteppedUp());

        if (user.isNewUser()) {
            user.setNewUser(false);
            userRepository.save(user);

            logWithContext(user, "add", "account", LOG, "Saving user after new registration and magic link");
            mailBox.sendAccountConfirmation(user);

            String url = this.redirectUrl + "/confirm?h=" + hash +
                    "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet) +
                    "&email=" + URLEncoder.encode(user.getEmail(), charSet) +
                    "&name=" + encodedServiceName;
            if (!StepUpStatus.NONE.equals(samlAuthenticationRequest.getSteppedUp())) {
                url += "&explanation=" + explanation;
            }
            response.sendRedirect(url);
            if (inStepUpFlow) {
                finishStepUp(samlAuthenticationRequest);
            }
            return false;
        } else if (inStepUpFlow) {
            boolean hasStudentAffiliation = hasRequiredStudentAffiliation(user.allEduPersonAffiliations());
            List<String> authenticationContextClassReferences = samlAuthenticationRequest.getAuthenticationContextClassReferences();
            boolean missingStudentAffiliation = authenticationContextClassReferences.contains(ACR.AFFILIATION_STUDENT) &&
                    !hasStudentAffiliation;
            boolean missingValidName = authenticationContextClassReferences.contains(ACR.VALIDATE_NAMES) &&
                    !hasValidatedName(user);
            if (missingStudentAffiliation || missingValidName) {
                //When we send the assertion EB stops the flow but this will be fixed upstream
                return true;
            } else {
                response.sendRedirect(this.redirectUrl + "/confirm-stepup?h=" + hash +
                        "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet) +
                        "&name=" + encodedServiceName +
                        "&explanation=" + explanation);
                finishStepUp(samlAuthenticationRequest);
                return false;
            }
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
            response.sendRedirect(this.redirectUrl + "/expired");
            return;
        }
        if (samlAuthenticationRequest.getLoginStatus().equals(LoginStatus.NOT_LOGGED_IN)) {
            response.sendRedirect(this.redirectUrl + "/expired");
            return;
        }
        User user = userRepository.findById(samlAuthenticationRequest.getUserId())
                .orElseThrow(UserNotFoundException::new);

        String charSet = Charset.defaultCharset().name();
        String serviceName = this.getServiceName(request, samlAuthenticationRequest);
        String encodedServiceName = URLEncoder.encode(serviceName, "UTF-8");
        boolean hasStudentAffiliation = hasRequiredStudentAffiliation(user.allEduPersonAffiliations());
        List<String> authenticationContextClassReferences = samlAuthenticationRequest.getAuthenticationContextClassReferences();
        String explanation = ACR.explanationKeyWord(authenticationContextClassReferences, hasStudentAffiliation);

        boolean proceed = this.checkStepUp(response, samlAuthenticationRequest.getHash(), samlAuthenticationRequest, user, charSet, encodedServiceName, explanation);
        if (!proceed) {
            return;
        }
        doSendAssertion(request, response, samlAuthenticationRequest, user);
    }

    private void doSendAssertion(HttpServletRequest request, HttpServletResponse response, SamlAuthenticationRequest samlAuthenticationRequest, User user) {
        //ensure the magic link can't be used twice
        samlAuthenticationRequest.setHash(null);

        LOG.debug(String.format("Disabling magic link after use by %s ", user.getUsername()));

        authenticationRequestRepository.save(samlAuthenticationRequest);

        IdentityProviderService provider = getProvisioning().getHostedProvider();
        ServiceProviderMetadata serviceProviderMetadata = provider.getRemoteProvider(samlAuthenticationRequest.getIssuer());

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setId(samlAuthenticationRequest.getRequestId());
        authenticationRequest.setAssertionConsumerService(new Endpoint().setLocation(samlAuthenticationRequest.getConsumerAssertionServiceURL()));

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        if (samlAuthenticationRequest.isRememberMe()) {
            LOG.info(String.format("Remember me functionality activated for %s ", user.getUsername()));
            addRememberMeCookie(response, samlAuthenticationRequest);
        }
        logLoginWithContext(user, "magiclink", true, LOG, "Successfully logged in with magiclink");
        sendAssertion(request, response, samlAuthenticationRequest, user, provider,
                serviceProviderMetadata, authenticationRequest);
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

    private void sendAssertion(HttpServletRequest request, HttpServletResponse response, SamlAuthenticationRequest samlAuthenticationRequest,
                               User user, IdentityProviderService provider, ServiceProviderMetadata serviceProviderMetadata,
                               AuthenticationRequest authenticationRequest) {
        String relayState = samlAuthenticationRequest.getRelayState();
        String requesterEntityId = samlAuthenticationRequest.getRequesterEntityId();
        Assertion assertion = provider.assertion(
                serviceProviderMetadata, authenticationRequest, user.getUid(), NameId.PERSISTENT);

        List<String> authenticationContextClassReferences = samlAuthenticationRequest.getAuthenticationContextClassReferences();
        attributes(user, requesterEntityId, authenticationContextClassReferences).forEach(assertion::addAttribute);

        Response samlResponse = provider.response(authenticationRequest, assertion, serviceProviderMetadata);

        if (samlAuthenticationRequest.isAccountLinkingRequired()) {
            boolean hasStudentAffiliation = hasRequiredStudentAffiliation(user.allEduPersonAffiliations());

            //When we change the status of the response to NO_AUTH_CONTEXT EB stops the flow but this will be fixed upstream
            boolean missingStudentAffiliation = authenticationContextClassReferences.contains(ACR.AFFILIATION_STUDENT) &&
                    !hasStudentAffiliation;
            boolean missingValidName = authenticationContextClassReferences.contains(ACR.VALIDATE_NAMES) &&
                    !hasValidatedName(user);
            if (missingStudentAffiliation || missingValidName) {
                String msg;
                if (missingValidName) {
                    msg = "The requesting service has indicated that the authenticated user is required to have a first_name and last_name." +
                            " Your institution has not provided those attributes.";
                } else {
                    msg = "The requesting service has indicated that the authenticated user is required to have an affiliation Student." +
                            " Your institution has not provided this affiliation.";
                }
                samlResponse.setStatus(new Status()
                        .setCode(StatusCode.NO_AUTH_CONTEXT)
                        .setMessage(msg)
                        .setDetail(msg));
            } else {
                samlResponse.getAssertions().get(0).getAuthenticationStatements().get(0)
                        .getAuthenticationContext()
                        .setClassReference(AuthenticationContextClassReference
                                .fromUrn(ACR.selectACR(authenticationContextClassReferences, hasStudentAffiliation)));
            }
        } else if (!CollectionUtils.isEmpty(authenticationContextClassReferences)) {
            String msg = String.format("The specified authentication context requirements '%s' cannot be met by the responder.",
                    String.join(", ", authenticationContextClassReferences));
            samlResponse.setStatus(new Status()
                    .setCode(StatusCode.NO_AUTH_CONTEXT)
                    .setMessage(msg)
                    .setDetail(msg));
        }
        Endpoint acsUrl = provider.getPreferredEndpoint(
                serviceProviderMetadata.getServiceProvider().getAssertionConsumerService(),
                Binding.POST,
                -1
        );
        String encoded = provider.toEncodedXml(samlResponse, false);
        Map<String, Object> model = new HashMap<>();
        model.put("action", acsUrl.getLocation());
        model.put("SAMLResponse", encoded);
        if (hasText(relayState)) {
            model.put("RelayState", HtmlUtils.htmlEscape(relayState));
        }
        Optional<Cookie> optionalCookie = cookieByName(request, BROWSER_SESSION_COOKIE_NAME);
        optionalCookie.ifPresent(cookie -> {
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        });
        processHtml(request, response, getPostBindingTemplate(), model);
    }

    protected List<Attribute> attributes(User user, String requesterEntityId, List<String> authenticationContextClassReferences) {
        List<LinkedAccount> linkedAccounts = safeSortedAffiliations(user);
        String givenName = user.getGivenName();
        String familyName = user.getFamilyName();

        if (authenticationContextClassReferences.contains(ACR.VALIDATE_NAMES) && !CollectionUtils.isEmpty(linkedAccounts)) {
            Optional<LinkedAccount> first = linkedAccounts.stream()
                    .filter(LinkedAccount::areNamesValidated).findFirst();
            //Can't use non-final variables in lambda
            if (first.isPresent()) {
                LinkedAccount linkedAccount = first.get();
                givenName = linkedAccount.getGivenName();
                familyName = linkedAccount.getFamilyName();
            }
        }

        String displayName = String.format("%s %s", givenName, familyName);
        List<Attribute> attributes = new ArrayList(Arrays.asList(
                attribute("urn:mace:dir:attribute-def:cn", displayName),
                attribute("urn:mace:dir:attribute-def:displayName", displayName),
                attribute("urn:mace:dir:attribute-def:eduPersonPrincipalName", user.getEduPersonPrincipalName()),
                attribute("urn:mace:dir:attribute-def:givenName", givenName),
                attribute("urn:mace:dir:attribute-def:mail", user.getEmail()),
                attribute("urn:mace:dir:attribute-def:sn", familyName),
                attribute("urn:mace:dir:attribute-def:uid", user.getUid()),
                attribute("urn:mace:terena.org:attribute-def:schacHomeOrganization", user.getSchacHomeOrganization())
        ));
        user.computeEduIdForServiceProviderIfAbsent(requesterEntityId, serviceProviderResolver);
        userRepository.save(user);

        String eduID = user.getEduIdPerServiceProvider().get(requesterEntityId).getValue();
        attributes.add(attribute("urn:mace:eduid.nl:1.1", eduID));

        user.getAttributes()
                .forEach((key, value) -> attributes.add(attribute(key, value.toArray(new String[]{}))));

        List<String> scopedAffiliations = linkedAccounts.stream()
                .map(linkedAccount -> linkedAccount.getEduPersonAffiliations().stream()
                        .map(affiliation -> affiliation.contains("@")
                                ? affiliation : String.format("%s@%s", affiliation, linkedAccount.getSchacHomeOrganization()))
                        .collect(toList()))
                .flatMap(Collection::stream)
                .collect(toList());

        if (!CollectionUtils.isEmpty(scopedAffiliations)) {
            attributes.add(attribute("urn:mace:dir:attribute-def:eduPersonScopedAffiliation",
                    scopedAffiliations.toArray(new String[]{})));
        }
        return attributes;
    }

    private List<LinkedAccount> safeSortedAffiliations(User user) {
        List<LinkedAccount> linkedAccounts = user.linkedAccountsSorted();
        List<LinkedAccount> linkedAccountsEmptyAffiliations = linkedAccounts.stream()
                .filter(linkedAccount -> CollectionUtils.isEmpty(linkedAccount.getEduPersonAffiliations()))
                .collect(toList());
        linkedAccountsEmptyAffiliations.forEach(linkedAccount -> linkedAccount.setEduPersonAffiliations(Arrays.asList("affiliation")));
        if (!linkedAccountsEmptyAffiliations.isEmpty()) {
            userRepository.save(user);
        }
        return user.linkedAccountsSorted();
    }

    private Attribute attribute(String name, String... value) {
        return new Attribute().setName(name).setNameFormat(AttributeNameFormat.URI).addValues((Object[]) value);
    }

}
