package myconext.security;

import myconext.exceptions.UserNotFoundException;
import myconext.mail.MailBox;
import myconext.manage.ServiceNameResolver;
import myconext.model.LinkedAccount;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
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
import org.springframework.security.saml.saml2.authentication.Assertion;
import org.springframework.security.saml.saml2.authentication.AuthenticationContextClassReference;
import org.springframework.security.saml.saml2.authentication.AuthenticationRequest;
import org.springframework.security.saml.saml2.authentication.Response;
import org.springframework.security.saml.saml2.authentication.Scoping;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
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
    private final String redirectUrl;
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;
    private final List<String> accountLinkingContextClassReferences;

    private final int rememberMeMaxAge;
    private final boolean secureCookie;
    private final String magicLinkUrl;
    private final MailBox mailBox;
    private final ServiceNameResolver serviceNameResolver;

    public GuestIdpAuthenticationRequestFilter(SamlProviderProvisioning<IdentityProviderService> provisioning,
                                               SamlMessageStore<Assertion, HttpServletRequest> assertionStore,
                                               String redirectUrl,
                                               ServiceNameResolver serviceNameResolver,
                                               AuthenticationRequestRepository authenticationRequestRepository,
                                               UserRepository userRepository,
                                               int rememberMeMaxAge,
                                               boolean secureCookie,
                                               String magicLinkUrl,
                                               MailBox mailBox) {
        super(provisioning, assertionStore);
        this.ssoSamlRequestMatcher = new SamlRequestMatcher(provisioning, "SSO");
        this.magicSamlRequestMatcher = new SamlRequestMatcher(provisioning, "magic");
        this.redirectUrl = redirectUrl;
        this.serviceNameResolver = serviceNameResolver;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.accountLinkingContextClassReferences =  Stream.of(ACR.values()).map(ACR::getValue).collect(Collectors.toList());
        this.rememberMeMaxAge = rememberMeMaxAge;
        this.secureCookie = secureCookie;
        this.magicLinkUrl = magicLinkUrl;
        this.mailBox = mailBox;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (this.ssoSamlRequestMatcher.matches(request)) {
            LOG.debug("Starting SSO filter");
            sso(request, response);
            return;
        } else if (this.magicSamlRequestMatcher.matches(request)) {
            LOG.debug("Starting magic filter");
            magic(request, response);
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
        List<String> authenticationContextClassReferenceValues = getAuthenticationContextClassReferenceValue(authenticationRequest);

        boolean accountLinkingRequired = isAccountLinkingRequired(authenticationContextClassReferenceValues);

        SamlAuthenticationRequest samlAuthenticationRequest = new SamlAuthenticationRequest(
                authenticationRequest.getId(),
                issuer,
                authenticationRequest.getAssertionConsumerService().getLocation(),
                relayState,
                StringUtils.hasText(requesterEntityId) ? requesterEntityId : "",
                accountLinkingRequired,
                authenticationContextClassReferenceValues.stream()
                        .filter(val -> this.accountLinkingContextClassReferences.contains(val)).findFirst().orElse(null)
        );

        // Use the returned instance for further operations as the save operation has added the _id
        samlAuthenticationRequest = authenticationRequestRepository.save(samlAuthenticationRequest);

        Optional<Cookie> rememberMeCookieOptional = cookieByName(request, GUEST_IDP_REMEMBER_ME_COOKIE_NAME);
        Optional<User> userRememberedOptional = rememberMeCookieOptional.map(this::userFromCookie).flatMap(identity());

        Optional<User> userFromAuthentication = userFromAuthentication();

        User previousAuthenticatedUser = userRememberedOptional.orElse(userFromAuthentication.orElse(null));

        String encodedServiceName = URLEncoder.encode(serviceNameResolver.resolve(requesterEntityId), "UTF-8");

        if (previousAuthenticatedUser != null && !authenticationRequest.isForceAuth()) {
            if (accountLinkingRequired && !isUserVerifiedByInstitution(previousAuthenticatedUser)) {
                response.sendRedirect(this.redirectUrl + "/stepup/" + samlAuthenticationRequest.getId() + "?name=" + encodedServiceName + "&existing=true");
            } else {
                ServiceProviderMetadata serviceProviderMetadata = provider.getRemoteProvider(samlAuthenticationRequest.getIssuer());
                sendAssertion(request, response, samlAuthenticationRequest, previousAuthenticatedUser,
                        provider, serviceProviderMetadata, authenticationRequest);
            }
        } else {
            addBrowserIdentificationCookie(response);
            String modus = cookieByName(request, REGISTER_MODUS_COOKIE_NAME).map(c -> "&modus=cr").orElse("");
            String stepUp = accountLinkingRequired ? "&stepup=true" : "";
            response.sendRedirect(this.redirectUrl + "/login/" + samlAuthenticationRequest.getId() +
                    "?name=" + encodedServiceName + modus + stepUp);
        }
    }

    private boolean isAccountLinkingRequired(List<String> authenticationContextClassReferenceValues) {
        return authenticationContextClassReferenceValues.stream().anyMatch(ref -> this.accountLinkingContextClassReferences.contains(ref));
    }

    public static boolean isUserVerifiedByInstitution(User user) {
        Date now = new Date();
        List<LinkedAccount> linkedAccounts = user.getLinkedAccounts();
        return !CollectionUtils.isEmpty(linkedAccounts) &&
                linkedAccounts.stream().allMatch(linkedAccount -> now.toInstant().isBefore(linkedAccount.getExpiresAt().toInstant()));
    }

    private List<String> getAuthenticationContextClassReferenceValue(AuthenticationRequest authenticationRequest) {
        List<AuthenticationContextClassReference> authenticationContextClassReferences = authenticationRequest.getAuthenticationContextClassReferences();
        return CollectionUtils.isEmpty(authenticationContextClassReferences) ? Collections.emptyList() :
                authenticationContextClassReferences.stream().map(ref -> ref.getValue()).collect(Collectors.toList());
    }

    private Optional<User> userFromCookie(Cookie remembered) {
        LOG.info("Returning user from rememberMe cookie");
        return authenticationRequestRepository.findByRememberMeValue(remembered.getValue())
                .map(req -> userRepository.findById(req.getUserId())).flatMap(identity());
    }

    private Optional<User> userFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        LOG.info("Attempting user authentication from security context: " + authentication);
        if (authentication != null && authentication.isAuthenticated() && authentication instanceof  UsernamePasswordAuthenticationToken) {
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
        Scoping scoping = authenticationRequest.getScoping();
        List<String> requesterIds = scoping != null ? scoping.getRequesterIds() : null;
        return CollectionUtils.isEmpty(requesterIds) ? null : requesterIds.get(0);
    }

    private void magic(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Optional<Cookie> optionalCookie = cookieByName(request, BROWSER_SESSION_COOKIE_NAME);
        if (!optionalCookie.isPresent()) {
            response.sendRedirect(this.redirectUrl + "/session");
            return;
        }
        String hash = request.getParameter("h");
        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findByHash(hash);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            response.sendRedirect(this.redirectUrl + "/expired");
            return;
        }
        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();
        User user = userRepository.findById(samlAuthenticationRequest.getUserId())
                .orElseThrow(UserNotFoundException::new);

        boolean accountLinkingRequired = samlAuthenticationRequest.isAccountLinkingRequired() &&
                !GuestIdpAuthenticationRequestFilter.isUserVerifiedByInstitution(user);

        String charSet = Charset.defaultCharset().name();
        String encodedServiceName = URLEncoder.encode(serviceNameResolver.resolve(samlAuthenticationRequest.getRequesterEntityId()), charSet);
        if (accountLinkingRequired) {
            response.sendRedirect(this.redirectUrl + "/stepup/" + samlAuthenticationRequest.getId()
                    + "?name=" + encodedServiceName + "&existing=true");
            return;
        }

        if (user.isNewUser()) {
            user.setNewUser(false);
            userRepository.save(user);

            LOG.info(String.format("Saving user %s after new registration and magic link", user.getUsername()));

            mailBox.sendAccountConfirmation(user);

            response.sendRedirect(this.redirectUrl + "/confirm?h=" + hash +
                    "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet) +
                    "&email=" + URLEncoder.encode(user.getEmail(), charSet) +
                    "&name=" + URLEncoder.encode(serviceNameResolver.resolve(samlAuthenticationRequest.getRequesterEntityId()), charSet));
            return;
        }
        //ensure the magic link can't be used twice
        samlAuthenticationRequest.setHash(null);

        LOG.info(String.format("Disabling magic link after use by %s ", user.getUsername()));

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

        sendAssertion(request, response, samlAuthenticationRequest, user, provider,
                serviceProviderMetadata, authenticationRequest);
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

        String authenticationContextClassReference = samlAuthenticationRequest.getAuthenticationContextClassReference();
        attributes(user, requesterEntityId, authenticationContextClassReference).forEach(assertion::addAttribute);

        Response samlResponse = provider.response(authenticationRequest, assertion, serviceProviderMetadata);

        if (samlAuthenticationRequest.isAccountLinkingRequired()) {
            samlResponse.getAssertions().get(0).getAuthenticationStatements().get(0)
                    .getAuthenticationContext()
                    .setClassReference(AuthenticationContextClassReference.fromUrn(authenticationContextClassReference));
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
        processHtml(request, response, getPostBindingTemplate(), model);
    }

    private List<Attribute> attributes(User user, String requesterEntityId, String authenticationContextClassReference) {
        List<LinkedAccount> linkedAccounts = user.linkedAccountsSorted();
        String givenName = user.getGivenName();
        String familyName = user.getFamilyName();

        if (ACR.VALIDATE_NAMES.getValue().equals(authenticationContextClassReference) && !CollectionUtils.isEmpty(linkedAccounts)) {
            LinkedAccount linkedAccount = linkedAccounts.get(0);
            if (linkedAccount.areNamesValidated()) {
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
        if (!user.getEduIdPerServiceProvider().containsKey(requesterEntityId)) {
            user.computeEduIdForServiceProviderIfAbsent(requesterEntityId);
            userRepository.save(user);
        }
        String eduID = user.getEduIdPerServiceProvider().get(requesterEntityId);
        attributes.add(attribute("urn:mace:eduid.nl:1.1", eduID));

        user.getAttributes()
                .forEach((key, value) -> attributes.add(attribute(key, value.toArray(new String[]{}))));
        //Use case for the student mobility intake app
        List<String> scopedAffiliations = linkedAccounts.stream()
                .map(linkedAccount ->
                        String.format("student@%s",
                                linkedAccount.getSchacHomeOrganization()))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(scopedAffiliations)) {
            attributes.add(attribute("urn:mace:dir:attribute-def:eduPersonScopedAffiliation",
                    scopedAffiliations.toArray(new String[]{})));
        }
        return attributes;
    }

    private Attribute attribute(String name, String... value) {
        return new Attribute().setName(name).setNameFormat(AttributeNameFormat.URI).addValues((Object[]) value);
    }

}
