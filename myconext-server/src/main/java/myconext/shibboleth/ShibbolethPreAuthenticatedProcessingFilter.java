package myconext.shibboleth;


import myconext.exceptions.MigrationDuplicateUserEmailException;
import myconext.mail.MailBox;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.stream.Stream;

import static myconext.log.MDCContext.mdcContext;
import static myconext.security.CookieResolver.cookieByName;

public class ShibbolethPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ShibbolethPreAuthenticatedProcessingFilter.class);

    public static final String SHIB_GIVEN_NAME = "Shib-givenName";
    public static final String SHIB_SUR_NAME = "Shib-surName";
    public static final String SHIB_EMAIL = "Shib-InetOrgPerson-mail";
    public static final String SHIB_UID = "uid";
    public static final String SHIB_SCHAC_HOME_ORGANIZATION = "schacHomeOrganization";
    public static final String SHIB_AUTHENTICATING_AUTHORITY = "Shib-Authenticating-Authority";

    private final UserRepository userRepository;
    private final MailBox mailBox;
    private final String oneginiEntityId;

    public ShibbolethPreAuthenticatedProcessingFilter(AuthenticationManager authenticationManager,
                                                      UserRepository userRepository,
                                                      String oneginiEntityId,
                                                      MailBox mailBox) {
        super();
        super.setAuthenticationManager(authenticationManager);
        this.userRepository = userRepository;
        this.mailBox = mailBox;
        this.oneginiEntityId = oneginiEntityId;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        String uid = getHeader(SHIB_UID, request);
        String schacHomeOrganization = getHeader(SHIB_SCHAC_HOME_ORGANIZATION, request);
        String email = getHeader(SHIB_EMAIL, request);
        String givenName = getHeader(SHIB_GIVEN_NAME, request);
        String familyName = getHeader(SHIB_SUR_NAME, request);
        String authenticatingAuthorities = getHeader(SHIB_AUTHENTICATING_AUTHORITY, request);

        boolean valid = Stream.of(uid, schacHomeOrganization, email, givenName, familyName, authenticatingAuthorities).allMatch(StringUtils::hasText);
        if (!valid) {
            //this is the contract. See AbstractPreAuthenticatedProcessingFilter#doAuthenticate
            LOG.warn("Missing required attribute(s): uid {}, schacHomeOrganization {}, givenName {}, familyName {}, email {}, authenticatingAuthorities {}",
                    uid, schacHomeOrganization, givenName, familyName, email, authenticatingAuthorities);
            return null;
        }
        Optional<User> optionalUser = userRepository.findUserByUid(uid);
        if (!optionalUser.isPresent()) {
            Optional<User> optionalUserByEmail = userRepository.findUserByEmailIgnoreCase(email);
            if (optionalUserByEmail.isPresent()) {
                User existingUser = optionalUserByEmail.get();
                //If we would provision this email we would introduce a duplicate email
                String requestURI = request.getRequestURI();
                mdcContext(optionalUser, "action", requestURI);
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
        //The authenticatingAuthority in the SAML / Shibd heading is a ';' separated list
        String authenticatingAuthority = authenticatingAuthorities.split(";")[0].trim();
        String preferredLanguage = cookieByName(request, "lang").map(Cookie::getValue).orElse("en");
        return optionalUser.orElseGet(() ->
                provisionUser(uid, schacHomeOrganization, givenName, familyName, email, authenticatingAuthority, preferredLanguage));
    }

    private User provisionUser(String uid, String schacHomeOrganization, String givenName, String familyName, String email, String authenticatingAuthority, String preferredLanguage) {
        User user = new User(uid, email, givenName, familyName, schacHomeOrganization, authenticatingAuthority, null, preferredLanguage);
        user.setNewUser(false);
        user = userRepository.save(user);


        boolean isOneGini = oneginiEntityId.equalsIgnoreCase(user.getAuthenticatingAuthority());

        String action = isOneGini ? "Sending account migration mail" : "Not sending account migration mail";
        mdcContext(Optional.of(user), "action", action, "authenticating_authority", authenticatingAuthority);
        LOG.info("Provision new user");

        if (isOneGini) {
            mailBox.sendAccountMigration(user);
        }
        return user;
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return "N/A";
    }

    private String getHeader(String name, HttpServletRequest request) {
        String header = request.getHeader(name);
        try {
            return StringUtils.hasText(header) ?
                    new String(header.getBytes("ISO8859-1"), "UTF-8") : "";
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }


}
