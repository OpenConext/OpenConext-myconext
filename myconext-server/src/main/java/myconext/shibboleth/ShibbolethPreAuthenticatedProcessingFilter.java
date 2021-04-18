package myconext.shibboleth;


import myconext.exceptions.MigrationDuplicateUserEmailException;
import myconext.manage.ServiceProviderResolver;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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

    private final UserRepository userRepository;
    private final ServiceProviderResolver serviceProviderResolver;
    private final String mijnEduIDEntityId;

    public ShibbolethPreAuthenticatedProcessingFilter(AuthenticationManager authenticationManager,
                                                      UserRepository userRepository,
                                                      ServiceProviderResolver serviceProviderResolver,
                                                      String mijnEduIDEntityId) {
        super();
        super.setAuthenticationManager(authenticationManager);
        this.userRepository = userRepository;
        this.serviceProviderResolver = serviceProviderResolver;
        this.mijnEduIDEntityId = mijnEduIDEntityId;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String uid = getHeader(SHIB_UID, request);
        String schacHomeOrganization = getHeader(SHIB_SCHAC_HOME_ORGANIZATION, request);
        String email = getHeader(SHIB_EMAIL, request);
        String givenName = getHeader(SHIB_GIVEN_NAME, request);
        String familyName = getHeader(SHIB_SUR_NAME, request);

        boolean valid = Stream.of(uid, schacHomeOrganization, email, givenName, familyName).allMatch(StringUtils::hasText);
        if (!valid) {
            //this is the contract. See AbstractPreAuthenticatedProcessingFilter#doAuthenticate
            LOG.warn(String.format("Missing required attribute(s): uid %s, schacHomeOrganization %s, givenName %s, familyName %s, email %s",
                    uid, schacHomeOrganization, givenName, familyName, email));
            return null;
        }
        Optional<User> optionalUser = userRepository.findUserByUid(uid);
        if (!optionalUser.isPresent()) {
            Optional<User> optionalUserByEmail = userRepository.findUserByEmail(email.trim());
            if (optionalUserByEmail.isPresent()) {
                User existingUser = optionalUserByEmail.get();
                //If we would provision this email we would introduce a duplicate email
                String requestURI = request.getRequestURI();
                if (requestURI.endsWith("sp/migrate/merge")) {
                    //We will provision a new user, so we delete the current one
                    LOG.info("Migrate oneGini account to eduID account");
                    userRepository.delete(existingUser);
                } else if (requestURI.endsWith("sp/migrate/proceed")) {
                    //Now discard everything from the IdP and use the current account
                    LOG.info("Not migrating oneGini account to eduID account");
                    optionalUser = optionalUserByEmail;
                } else {
                    //need to be picked up by the client
                    throw new MigrationDuplicateUserEmailException(email, request.getRequestURI());
                }
            }
        }
        String preferredLanguage = cookieByName(request, "lang").map(Cookie::getValue).orElse("en");
        return optionalUser.orElseGet(() ->
                provisionUser(uid, schacHomeOrganization, givenName, familyName, email, preferredLanguage));
    }

    private User provisionUser(String uid, String schacHomeOrganization, String givenName, String familyName,
                               String email, String preferredLanguage) {
        User user = new User(uid, email, givenName, familyName, schacHomeOrganization, preferredLanguage, mijnEduIDEntityId, serviceProviderResolver);
        user.setNewUser(false);
        user = userRepository.save(user);

        logWithContext(user, "add", "user",LOG, String.format("Provisioned new user %s", user.getEmail()));

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
