package myconext.security;

import myconext.AbstractIntegrationTest;
import myconext.exceptions.UserNotFoundException;
import myconext.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserAuthenticationTest extends AbstractIntegrationTest implements UserAuthentication {

    //See src/test/resources/users.json
    private static final String VALID_UID = "1234567890";

    @Test
    void testUserFromAuthentication() {
        BearerTokenAuthentication tokenAuthentication = getBearerTokenAuthentication(VALID_UID);
        User user = userFromAuthentication(tokenAuthentication);
        assertEquals(VALID_UID, user.getUid());
    }

    @Test
    void testUserFromAuthenticationId() {
        User user = userRepository.findUserByUid(VALID_UID).get();
        TestingAuthenticationToken tokenAuthentication = testingAuthenticationTokenForUserId(user.getId());
        User userFromContext = userFromAuthentication(tokenAuthentication);
        assertEquals(VALID_UID, userFromContext.getUid());
    }

    @Test
    void testUserFromAuthenticationIdNotFound() {
        String unknownId = UUID.randomUUID().toString();
        TestingAuthenticationToken tokenAuthentication = testingAuthenticationTokenForUserId(unknownId);
        assertThrows(UserNotFoundException.class, () -> userFromAuthentication(tokenAuthentication));
    }

    @Test
    void testUserFromAuthenticationNotFound() {
        BearerTokenAuthentication tokenAuthentication = getBearerTokenAuthentication("nope");
        assertThrows(UserNotFoundException.class, () -> userFromAuthentication(tokenAuthentication));
    }
    
    private static DefaultOidcUser oidcUserWithIdClaim(String userId) {
        Map<String, Object> claims = Map.of(
                "id", userId,
                "sub", userId);
        OidcIdToken idToken = new OidcIdToken(
                UUID.randomUUID().toString(),
                Instant.now(),
                Instant.now().plus(1, ChronoUnit.HOURS),
                claims);
        OidcUserInfo userInfo = new OidcUserInfo(claims);
        return new DefaultOidcUser(List.of(), idToken, userInfo);
    }

    private static TestingAuthenticationToken testingAuthenticationTokenForUserId(String userId) {
        return new TestingAuthenticationToken(oidcUserWithIdClaim(userId), "N/A");
    }

    private BearerTokenAuthentication getBearerTokenAuthentication(String uid) {
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ADMIN"));
        Map<String, Object> attributes = Map.of("uids", List.of(uid));
        OAuth2AuthenticatedPrincipal principal = new DefaultOAuth2User(authorities, attributes, "uids");
        OAuth2AccessToken credentials = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, UUID.randomUUID().toString(), Instant.now(), Instant.now().plus(100, ChronoUnit.DAYS));
        BearerTokenAuthentication tokenAuthentication = new BearerTokenAuthentication(principal, credentials, authorities);
        return tokenAuthentication;
    }
}
