package myconext.shibboleth;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import myconext.manage.Manage;
import myconext.model.ExternalUser;
import myconext.model.User;
import myconext.repository.ExternalUserRepository;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static myconext.log.MDCContext.logWithContext;
import static myconext.security.CookieResolver.cookieByName;

public class ShibbolethPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

    private static final Log LOG = LogFactory.getLog(ShibbolethPreAuthenticatedProcessingFilter.class);

    public static final String SHIB_GIVEN_NAME = "Shib-givenName";
    public static final String SHIB_SUR_NAME = "Shib-surName";
    public static final String SHIB_EMAIL = "Shib-InetOrgPerson-mail";
    public static final String SHIB_UID = "uid";
    public static final String SHIB_SCHAC_HOME_ORGANIZATION = "schacHomeOrganization";
    public static final String SHIB_MEMBERSHIPS = "is-member-of";

    private final UserRepository userRepository;
    private final ExternalUserRepository externalUserRepository;
    private final Manage serviceProviderResolver;
    private final String mijnEduIDEntityId;
    private final List<String> serviceDeskRoles;
    private final String mijnEduIDHost;
    private final String serviceDeskHost;

    public ShibbolethPreAuthenticatedProcessingFilter(AuthenticationManager authenticationManager,
                                                      UserRepository userRepository,
                                                      ExternalUserRepository externalUserRepository,
                                                      Manage serviceProviderResolver,
                                                      String mijnEduIDEntityId,
                                                      List<String> serviceDeskRoles,
                                                      String mijnEduIDHost,
                                                      String serviceDeskHost) {
        super();
        super.setAuthenticationManager(authenticationManager);
        this.userRepository = userRepository;
        this.externalUserRepository = externalUserRepository;
        this.serviceProviderResolver = serviceProviderResolver;
        this.mijnEduIDEntityId = mijnEduIDEntityId;
        this.serviceDeskRoles = serviceDeskRoles;
        this.mijnEduIDHost = mijnEduIDHost.toLowerCase();
        this.serviceDeskHost = serviceDeskHost.toLowerCase();
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        if (LOG.isTraceEnabled()) {
            Enumeration<String> headerNames = request.getHeaderNames();
            if (headerNames != null && !request.getRequestURI().endsWith("health") && !request.getRequestURI().endsWith("ico")) {
                Collections.list(headerNames).forEach(s -> {
                    LOG.trace(String.format("Header %s, value: %s", s, request.getHeader(s)));
                });
            }
        }
        String uid = getHeader(SHIB_UID, request);
        String schacHomeOrganization = getHeader(SHIB_SCHAC_HOME_ORGANIZATION, request);
        String email = getHeader(SHIB_EMAIL, request);
        String givenName = getHeader(SHIB_GIVEN_NAME, request);
        String familyName = getHeader(SHIB_SUR_NAME, request);

        boolean valid = Stream.of(uid, schacHomeOrganization, email, givenName, familyName).allMatch(StringUtils::hasText);
        if (valid) {
            LOG.info(String.format("Required attribute(s) present in shib headers: uid '%s', schacHomeOrganization '%s', givenName '%s', familyName '%s', email '%s'",
                    uid, schacHomeOrganization, givenName, familyName, email));
        } else {
            //this is the contract. See AbstractPreAuthenticatedProcessingFilter#doAuthenticate
            LOG.warn(String.format("Missing required attribute(s): uid '%s', schacHomeOrganization '%s', givenName '%s', familyName '%s', email '%s'",
                    uid, schacHomeOrganization, givenName, familyName, email));
            return null;
        }
        String host = getHeader("host", request);
        if (!StringUtils.hasText(host)) {
            throw new IllegalArgumentException("There is no host header in the request");
        }
        boolean logInToEduID = host.toLowerCase().equals(this.mijnEduIDHost);
        boolean logInToServiceDesk = host.toLowerCase().equals(this.serviceDeskHost);
        Optional<User> optionalUser = null;
        Optional<ExternalUser> optionalExternalUser = null;

        if (logInToEduID) {
            optionalUser = userRepository.findUserByUid(uid);
        } else if (logInToServiceDesk) {
            optionalExternalUser = externalUserRepository.findUserByUid(uid);
        } else {
            throw new IllegalArgumentException("Unknown host header in the request: " + host);
        }
        String preferredLanguage = cookieByName(request, "lang").map(Cookie::getValue).orElse("en");
        List<String> memberships = Stream.of(getHeader(SHIB_MEMBERSHIPS, request).split(";"))
                .map(String::trim)
                .toList();
        if (logInToEduID) {
            return optionalUser.orElseGet(() ->
                    provisionUser(uid, schacHomeOrganization, givenName, familyName, email, preferredLanguage));
        } else {
            return optionalExternalUser.map(user -> syncMemberships(user, memberships)).orElseGet(() ->
                    provisionServiceDeskUser(uid, schacHomeOrganization, givenName, familyName, email, memberships));
        }
    }

    private ExternalUser syncMemberships(ExternalUser user, List<String> memberships) {
        boolean isServiceDeskMember = this.serviceDeskRoles.stream().anyMatch(memberships::contains);
        if (user.isServiceDeskMember() != isServiceDeskMember) {
            user.setServiceDeskMember(isServiceDeskMember);
            externalUserRepository.save(user);
        }
        return user;
    }

    private User provisionUser(String uid, String schacHomeOrganization, String givenName, String familyName,
                               String email, String preferredLanguage) {
        User user = new User(uid, email, givenName, givenName, familyName, schacHomeOrganization,
                preferredLanguage, mijnEduIDEntityId, serviceProviderResolver);
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

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

    private String getHeader(String name, HttpServletRequest request) {
        String header = request.getHeader(name);
        return StringUtils.hasText(header) ?
                new String(header.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8) : "";
    }


}
