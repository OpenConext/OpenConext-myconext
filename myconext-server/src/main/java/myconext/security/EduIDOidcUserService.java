package myconext.security;

import jakarta.servlet.http.HttpServletRequest;
import myconext.manage.Manage;
import myconext.model.ExternalUser;
import myconext.model.User;
import myconext.repository.ExternalUserRepository;
import myconext.repository.UserRepository;
import oidc.security.CustomOidcUserService;
import oidc.security.UserProvisioning;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static myconext.log.MDCContext.logWithContext;

public class EduIDOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private static final Log LOG = LogFactory.getLog(EduIDOidcUserService.class);

    private final Environment environment;
    private final Manage manage;
    private final UserRepository userRepository;
    private final ExternalUserRepository externalUserRepository;
    private final OidcUserService delegate;
    private final String mijnEduIDEntityId;
    private final String mijnEduIDHost;
    private final String serviceDeskHost;
    private  final String activeHost;
    private final List<String> serviceDeskRoles;

    public EduIDOidcUserService(
            Environment environment,
            Manage manage,
            UserRepository userRepository,
            ExternalUserRepository externalUserRepository,
            String mijnEduIDEntityId,
            String mijnEduIDHost,
            String serviceDeskHost,
            String activeHost,
            List<String> serviceDeskRoles
    ) {
        //This is the default
        this.environment = environment;
        this.manage = manage;
        this.userRepository = userRepository;
        this.externalUserRepository = externalUserRepository;
        this.delegate = new OidcUserService();
        this.mijnEduIDEntityId = mijnEduIDEntityId;
        this.mijnEduIDHost = mijnEduIDHost;
        this.serviceDeskHost = serviceDeskHost;
        this.activeHost = activeHost;
        this.serviceDeskRoles = serviceDeskRoles;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate to the default implementation for loading a user
        OidcUser oidcUser = delegate.loadUser(userRequest);
        Map<String, Object> claims = oidcUser.getUserInfo().getClaims();
        // We need a mutable Map instead of the returned immutable Map
        Map<String, Object> newClaims = new HashMap<>(claims);

        LOG.debug("Provision oidcUser: " + claims);

        // Get from claims
        String uid = claims.get("sub").toString();
        String schacHomeOrganization = claims.get("schac_home_organization").toString(); //getHeader(SHIB_SCHAC_HOME_ORGANIZATION, request);
        String email = claims.get("email_verified").toString(); //getHeader(SHIB_EMAIL, request);
        String givenName = claims.get("given_name").toString(); //getHeader(SHIB_GIVEN_NAME, request);
        String familyName = claims.get("family_name").toString(); //getHeader(SHIB_SUR_NAME, request);

        //Now provision the user orretrieve the user
        String host;
        if (environment.acceptsProfiles(Profiles.of("test", "dev"))) {
            host = activeHost;
        } else {
            host = getHeader("host");
        }

        if (!StringUtils.hasText(host)) {
            throw new IllegalArgumentException("There is no host header in the request");
        }
        boolean logInToEduID = host.toLowerCase().equals(this.mijnEduIDHost);
        boolean logInToServiceDesk = host.toLowerCase().equals(this.serviceDeskHost);
        Optional<User> optionalUser = Optional.empty();
        Optional<ExternalUser> optionalExternalUser = Optional.empty();

        if (logInToEduID) {
            optionalUser = userRepository.findUserByUid(uid);
        } else if (logInToServiceDesk) {
            optionalExternalUser = externalUserRepository.findUserByUid(uid);
        } else {
            throw new IllegalArgumentException("Unknown host header in the request: " + host);
        }



        // Any of the 2 types of users is fetched -- see the provision step in Shib filter
        // The endgame is to return a OidcUser at all times

        // TODO add the ID of the usesr (either existing or new user) to the claims
//        newClaims.put("sub", uid);
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

    private User provisionUser(String uid, String schacHomeOrganization, String givenName, String familyName,
                               String email, String preferredLanguage) {
        User user = new User(uid, email, givenName, givenName, familyName, schacHomeOrganization,
                preferredLanguage, mijnEduIDEntityId, manage);
        user.setNewUser(false);
        user = userRepository.save(user);

        logWithContext(user, "add", "user", LOG, String.format("Provisioned new user %s", user.getEmail()));

        return user;
    }

    private ExternalUser provisionServiceDeskUser(String uid, String schacHomeOrganization, String givenName, String familyName,
                                                  String email, List<String> memberships) {
        ExternalUser user = new ExternalUser(uid, email, givenName, familyName, schacHomeOrganization);
        boolean isServiceDeskMember = this.serviceDeskRoles.stream().anyMatch(memberships::contains);
        user.setServiceDeskMember(isServiceDeskMember);
        user.setNewUser(false);
        user = externalUserRepository.save(user);

        return user;
    }
}
