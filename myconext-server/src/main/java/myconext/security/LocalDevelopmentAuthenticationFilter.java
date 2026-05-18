package myconext.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import myconext.exceptions.UserNotFoundException;
import myconext.model.ExternalUser;
import myconext.model.User;
import myconext.repository.ExternalUserRepository;
import myconext.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static myconext.security.SecurityConfiguration.InternalSecurityConfigurationAdapter.SERVICE_DESK;

public class LocalDevelopmentAuthenticationFilter implements Filter {

    private static final String LOCAL_MARY_DOE_UID = "mdoe";
    private static final String LOCAL_JOHN_DOE_UID = "1234567890";

    private final UserRepository userRepository;
    private final ExternalUserRepository externalUserRepository;
    private final boolean serviceDeskRoleAutoProvisioning;

    public LocalDevelopmentAuthenticationFilter(UserRepository userRepository,
                                                ExternalUserRepository externalUserRepository,
                                                boolean serviceDeskRoleAutoProvisioning) {
        this.userRepository = userRepository;
        this.externalUserRepository = externalUserRepository;
        this.serviceDeskRoleAutoProvisioning = serviceDeskRoleAutoProvisioning;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        List<SimpleGrantedAuthority> authorities;
        Map<String, Object> claims;

        if (serviceDeskRoleAutoProvisioning) {
            authorities = List.of(new SimpleGrantedAuthority("OPENID"), new SimpleGrantedAuthority(SERVICE_DESK));
            ExternalUser maryDoe = externalUserRepository.findUserByUid(LOCAL_MARY_DOE_UID)
                    .orElseGet(this::provisionLocalMaryDoe);
            claims = createClaimsForMaryDoe(maryDoe);
        } else {
            authorities = List.of(new SimpleGrantedAuthority("OPENID"));
            User johnDoe = userRepository.findUserByUid(LOCAL_JOHN_DOE_UID)
                    .orElseThrow(() -> new UserNotFoundException(LOCAL_JOHN_DOE_UID));
            claims = createClaimsForJohnDoe(johnDoe);
        }

        OidcIdToken idtoken = new OidcIdToken(
                UUID.randomUUID().toString(),
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.HOURS),
                claims
        );
        OidcUserInfo userInfo = new OidcUserInfo(claims);
        DefaultOidcUser oidcUser = new DefaultOidcUser(authorities, idtoken, userInfo);
        OAuth2AuthenticationToken authenticationToken = new OAuth2AuthenticationToken(
                oidcUser,
                authorities,
                "oidcng"
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private ExternalUser provisionLocalMaryDoe() {
        ExternalUser maryDoe = new ExternalUser(LOCAL_MARY_DOE_UID, "mdoe@example.com", "Mary", "Doe", "example.com");
        maryDoe.setServiceDeskMember(true);
        maryDoe.setNewUser(false);
        return externalUserRepository.save(maryDoe);
    }

    private Map<String, Object> createClaimsForJohnDoe(User user) {
        return Map.ofEntries(
                Map.entry("id", user.getId()),
                Map.entry("eduperson_principal_name", "urn:collab:person:example.com:super"),
                Map.entry("email", "email"),
                Map.entry("family_name", "Doe"),
                Map.entry("given_name", "John"),
                Map.entry("name", "John Doe"),
                Map.entry("schac_home_organization", "example.com"),
                Map.entry("scope", "openid"),
                Map.entry("edumember_is_member_of", List.of("none-existing-role")),
                Map.entry("sub", "urn:collab:person:example.com:super"),
                Map.entry("uids", List.of("super")));
    }

    private Map<String, Object> createClaimsForMaryDoe(ExternalUser user) {
        return Map.ofEntries(
                Map.entry("id", user.getId()),
                Map.entry("eduperson_principal_name", user.getEduPersonPrincipalName()),
                Map.entry("email", user.getEmail()),
                Map.entry("family_name", user.getFamilyName()),
                Map.entry("given_name", user.getGivenName()),
                Map.entry("name", user.getName()),
                Map.entry("schac_home_organization", user.getSchacHomeOrganization()),
                Map.entry("scope", "openid"),
                Map.entry("edumember_is_member_of", List.of("role3")),
                Map.entry("sub", "urn:collab:person:" + user.getSchacHomeOrganization() + ":" + user.getUid()),
                Map.entry("uids", List.of(user.getUid())));
    }
}
