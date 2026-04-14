package myconext.security;

import jakarta.servlet.http.HttpServletRequest;
import myconext.model.ExternalUser;
import myconext.model.User;
import myconext.repository.UserRepository;
import oidc.security.CustomOidcUserService;
import oidc.security.UserProvisioning;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EduIDOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final Log LOG = LogFactory.getLog(EduIDOidcUserService.class);

    private final OidcUserService delegate;

    public EduIDOidcUserService(UserRepository userRepository) {
        //This is the default
        this.delegate = new OidcUserService();
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate to the default implementation for loading a user
        OidcUser oidcUser = delegate.loadUser(userRequest);
        Map<String, Object> claims = oidcUser.getUserInfo().getClaims();
        // We need a mutable Map instead of the returned immutable Map
        Map<String, Object> newClaims = new HashMap<>(claims);

        LOG.debug("Provision oidcUser: " + claims);

//        Get from claims

//        String uid = claims.get("sub")
//        String schacHomeOrganization = getHeader(SHIB_SCHAC_HOME_ORGANIZATION, request);
//        String email = getHeader(SHIB_EMAIL, request);
//        String givenName = getHeader(SHIB_GIVEN_NAME, request);
//        String familyName = getHeader(SHIB_SUR_NAME, request);

        //Now provision the user orretrieve the user

        String host = getHeader("host");
//        if (!StringUtils.hasText(host)) {
//            throw new IllegalArgumentException("There is no host header in the request");
//        }
//        boolean logInToEduID = host.toLowerCase().equals(this.mijnEduIDHost);
//        boolean logInToServiceDesk = host.toLowerCase().equals(this.serviceDeskHost);
//        Optional<User> optionalUser = Optional.empty();
//        Optional<ExternalUser> optionalExternalUser = Optional.empty();
//
//        if (logInToEduID) {
//            optionalUser = userRepository.findUserByUid(uid);
//        } else if (logInToServiceDesk) {
//            optionalExternalUser = externalUserRepository.findUserByUid(uid);
//        } else {
//            throw new IllegalArgumentException("Unknown host header in the request: " + host);
//        }
            //TODO add the ID of the usesr (either existing or new user) to the claims
        OidcUserInfo oidcUserInfo = new OidcUserInfo(newClaims);
        oidcUser = new DefaultOidcUser(oidcUser.getAuthorities(), oidcUser.getIdToken(), oidcUserInfo);
        return oidcUser;

    }

    private String getHeader(String name) {
        HttpServletRequest request = getRequest();
        String header = request.getHeader(name);
        return StringUtils.hasText(header) ?
                new String(header.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8) : "";
    }


    public HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes();
        return attrs.getRequest();
    }
}
