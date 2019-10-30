package surfid.security;

import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.saml.SamlMessageStore;
import org.springframework.security.saml.SamlRequestMatcher;
import org.springframework.security.saml.provider.identity.IdentityProviderService;
import org.springframework.security.saml.provider.identity.IdpAuthenticationRequestFilter;
import org.springframework.security.saml.provider.provisioning.SamlProviderProvisioning;
import org.springframework.security.saml.saml2.authentication.Assertion;
import org.springframework.security.saml.saml2.authentication.AuthenticationRequest;
import org.springframework.security.saml.saml2.metadata.ServiceProviderMetadata;
import surfid.model.SamlAuthenticationRequest;
import surfid.repository.AuthenticationRequestRepository;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

public class GuestIdpAuthenticationRequestFilter extends IdpAuthenticationRequestFilter {

    private final SamlRequestMatcher samlRequestMatcher;
    private final String redirectUrl;
    private final AuthenticationRequestRepository authenticationRequestRepository;

    public GuestIdpAuthenticationRequestFilter(SamlProviderProvisioning<IdentityProviderService> provisioning,
                                               SamlMessageStore<Assertion, HttpServletRequest> assertionStore,
                                               String redirectUrl,
                                               AuthenticationRequestRepository authenticationRequestRepository) {
        super(provisioning, assertionStore);
        this.samlRequestMatcher = new SamlRequestMatcher(provisioning, "SSO");
        this.redirectUrl = redirectUrl;
        this.authenticationRequestRepository = authenticationRequestRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (this.samlRequestMatcher.matches(request)) {
            String samlRequest = request.getParameter("SAMLRequest");
            String relayState = request.getParameter("RelayState");
            Date expiresIn = Date.from(LocalDateTime.now().plusHours(1).atZone(ZoneId.systemDefault()).toInstant());
            SamlAuthenticationRequest samlAuthenticationRequest = new SamlAuthenticationRequest(samlRequest, relayState);
            this.authenticationRequestRepository.insert(samlAuthenticationRequest);
            response.sendRedirect(this.redirectUrl + "/login/" + samlAuthenticationRequest.getId());
            return;
        }
        super.doFilterInternal(request, response, filterChain);
    }

    @Override
    protected Assertion getAssertion(Authentication authentication, AuthenticationRequest authenticationRequest, IdentityProviderService provider, ServiceProviderMetadata recipient) {
        return super.getAssertion(authentication, authenticationRequest, provider, recipient);
    }


    private AuthenticationRequest restoreAuthenticationRequest(HttpServletRequest request) {
        IdentityProviderService provider = getProvisioning().getHostedProvider();
        String param = request.getParameter("SAMLRequest");
        return
                provider.fromXml(
                        param,
                        true,
                        HttpMethod.GET.name().equalsIgnoreCase(request.getMethod()),
                        AuthenticationRequest.class
                );
    }

}
