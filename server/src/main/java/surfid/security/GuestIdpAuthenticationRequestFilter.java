package surfid.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml.SamlMessageStore;
import org.springframework.security.saml.SamlRequestMatcher;
import org.springframework.security.saml.provider.identity.IdentityProviderService;
import org.springframework.security.saml.provider.identity.IdpAuthenticationRequestFilter;
import org.springframework.security.saml.provider.provisioning.SamlProviderProvisioning;
import org.springframework.security.saml.saml2.attribute.Attribute;
import org.springframework.security.saml.saml2.attribute.AttributeNameFormat;
import org.springframework.security.saml.saml2.authentication.Assertion;
import org.springframework.security.saml.saml2.authentication.AuthenticationRequest;
import org.springframework.security.saml.saml2.authentication.Response;
import org.springframework.security.saml.saml2.metadata.Binding;
import org.springframework.security.saml.saml2.metadata.Endpoint;
import org.springframework.security.saml.saml2.metadata.NameId;
import org.springframework.security.saml.saml2.metadata.ServiceProviderMetadata;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import surfid.exceptions.ExpiredAuthenticationException;
import surfid.exceptions.UserNotFoundException;
import surfid.model.SamlAuthenticationRequest;
import surfid.model.User;
import surfid.repository.AuthenticationRequestRepository;
import surfid.repository.UserRepository;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.hasText;

public class GuestIdpAuthenticationRequestFilter extends IdpAuthenticationRequestFilter {

    private final SamlRequestMatcher ssoSamlRequestMatcher;
    private final SamlRequestMatcher magicSamlRequestMatcher;
    private final String redirectUrl;
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;

    public GuestIdpAuthenticationRequestFilter(SamlProviderProvisioning<IdentityProviderService> provisioning,
                                               SamlMessageStore<Assertion, HttpServletRequest> assertionStore,
                                               String redirectUrl,
                                               AuthenticationRequestRepository authenticationRequestRepository,
                                               UserRepository userRepository) {
        super(provisioning, assertionStore);
        this.ssoSamlRequestMatcher = new SamlRequestMatcher(provisioning, "SSO");
        this.magicSamlRequestMatcher = new SamlRequestMatcher(provisioning, "magic");
        this.redirectUrl = redirectUrl;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (this.ssoSamlRequestMatcher.matches(request)) {
            sso(request, response);
            return;
        } else if (this.magicSamlRequestMatcher.matches(request)) {
            magic(request, response);
            return;
        }
        super.doFilterInternal(request, response, filterChain);
    }

    private void magic(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String hash = request.getParameter("h");
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash).orElseThrow(ExpiredAuthenticationException::new);
        User user = userRepository.findById(samlAuthenticationRequest.getUserId()).orElseThrow(UserNotFoundException::new);

        IdentityProviderService provider = getProvisioning().getHostedProvider();
        // Use ServiceProviderMetadata from static content, but do validate - see getTargetProvider(request);
        ServiceProviderMetadata recipient = getTargetProvider(request);

        AuthenticationRequest authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setId(samlAuthenticationRequest.getRequestId());
        authenticationRequest.setAssertionConsumerService(new Endpoint().setLocation(samlAuthenticationRequest.getConsumerAssertionServiceURL()));


        Assertion assertion = provider.assertion(recipient, authenticationRequest, user.getEmail(), NameId.EMAIL);

        attributes(user).forEach(assertion::addAttribute);

        Response samlResponse = provider.response(authenticationRequest, assertion, recipient);

        Endpoint acsUrl = provider.getPreferredEndpoint(
                recipient.getServiceProvider().getAssertionConsumerService(),
                Binding.POST,
                -1
        );
        String relayState = samlAuthenticationRequest.getRelayState();
        if (acsUrl.getBinding() == Binding.REDIRECT) {
            String encoded = provider.toEncodedXml(samlResponse, true);
            UriComponentsBuilder url = UriComponentsBuilder.fromUriString(acsUrl.getLocation());
            url.queryParam("SAMLRequest", UriUtils.encode(encoded, StandardCharsets.UTF_8.name()));
            if (hasText(relayState)) {
                url.queryParam("RelayState", UriUtils.encode(relayState, StandardCharsets.UTF_8.name()));
            }
            String redirect = url.build(true).toUriString();
            response.sendRedirect(redirect);
        } else if (acsUrl.getBinding() == Binding.POST) {
            String encoded = provider.toEncodedXml(samlResponse, false);
            Map<String, Object> model = new HashMap<>();
            model.put("action", acsUrl.getLocation());
            model.put("SAMLResponse", encoded);
            if (hasText(relayState)) {
                model.put("RelayState", HtmlUtils.htmlEscape(relayState));
            }
            processHtml(request, response, getPostBindingTemplate(), model);
        }
    }

    private List<Attribute> attributes(User user) {
        String displayName = String.format("%s %s", user.getGivenName(), user.getFamilyName());
        return Arrays.asList(
                attribute("urn:mace:dir:attribute-def:cn", displayName),
                attribute("urn:mace:dir:attribute-def:displayName", displayName),
                attribute("urn:mace:dir:attribute-def:eduPersonPrincipalName", user.getEmail()),
                attribute("urn:mace:dir:attribute-def:givenName", user.getGivenName()),
                attribute("urn:mace:dir:attribute-def:mail", user.getEmail()),
                attribute("urn:mace:dir:attribute-def:sn", user.getFamilyName()),
                attribute("urn:mace:dir:attribute-def:uid", user.getEmail()),
                attribute("urn:mace:terena.org:attribute-def:schacHomeOrganization", "surf.guest.id")
        );
    }

    private Attribute attribute(String name, String value) {
        return new Attribute().setName(name).setNameFormat(AttributeNameFormat.URI).addValues(value);
    }

    private void sso(HttpServletRequest request, HttpServletResponse response) throws IOException {
        IdentityProviderService provider = getProvisioning().getHostedProvider();
        String samlRequest = request.getParameter("SAMLRequest");
        String relayState = request.getParameter("RelayState");

        AuthenticationRequest authenticationRequest =
                provider.fromXml(samlRequest, true, HttpMethod.GET.name().equalsIgnoreCase(request.getMethod()), AuthenticationRequest.class);

        SamlAuthenticationRequest samlAuthenticationRequest = new SamlAuthenticationRequest(
                authenticationRequest.getId(),
                authenticationRequest.getAssertionConsumerService().getLocation(),
                relayState);

        // Use the returned instance for further operations as the save operation has added the _id
        samlAuthenticationRequest = this.authenticationRequestRepository.save(samlAuthenticationRequest);
        response.sendRedirect(this.redirectUrl + "/login/" + samlAuthenticationRequest.getId());
    }

    @Override
    protected Assertion getAssertion(Authentication authentication, AuthenticationRequest authenticationRequest, IdentityProviderService provider, ServiceProviderMetadata recipient) {
        return provider.assertion(recipient, authenticationRequest, authentication.getName(), NameId.PERSISTENT);
    }

}
