package myconext.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import myconext.exceptions.UserNotFoundException;
import myconext.model.User;
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

    private final UserRepository userRepository;
    private final boolean serviceDeskRoleAutoProvisioning;

    public LocalDevelopmentAuthenticationFilter(UserRepository userRepository, boolean serviceDeskRoleAutoProvisioning) {
        this.userRepository = userRepository;
        this.serviceDeskRoleAutoProvisioning = serviceDeskRoleAutoProvisioning;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        List<SimpleGrantedAuthority> authorities = serviceDeskRoleAutoProvisioning
        ? List.of(new SimpleGrantedAuthority("OPENID"), new SimpleGrantedAuthority(SERVICE_DESK))
        : List.of(new SimpleGrantedAuthority("OPENID"));

        String userUidToFind = serviceDeskRoleAutoProvisioning ? "mdoe" :"1234567890";
        User user = userRepository.findUserByUid(
                userUidToFind
        ).orElseThrow(() -> new UserNotFoundException(userUidToFind));

        Map<String, Object> claims = /*serviceDeskRoleAutoProvisioning ? createClaimsForMaryDoe(user) :*/ createClaimsForJohnDoe(user);
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
                Map.entry("edumember_is_member_of", List.of(serviceDeskRoleAutoProvisioning ? "role3" : "none-existing-role")),
                Map.entry("sub", "urn:collab:person:example.com:super"),
                Map.entry("uids", List.of("super")));
    }
}
