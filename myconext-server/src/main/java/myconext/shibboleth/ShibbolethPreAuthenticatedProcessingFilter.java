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

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Optional;
import java.util.stream.Stream;

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
        setAuthenticationManager(authenticationManager);
        this.userRepository = userRepository;
        this.mailBox = mailBox;
        this.oneginiEntityId = oneginiEntityId;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
        String uid = getHeader(SHIB_UID, request);
        String schacHomeOrganization = getHeader(SHIB_SCHAC_HOME_ORGANIZATION, request);
        String email = getHeader(SHIB_EMAIL, request);
        String givenName = getHeader(SHIB_GIVEN_NAME, request);
        String familyName = getHeader(SHIB_SUR_NAME, request);
        String authenticatingAuthority = getHeader(SHIB_AUTHENTICATING_AUTHORITY, request);

        boolean valid = Stream.of(uid, schacHomeOrganization, email, givenName, familyName).allMatch(StringUtils::hasText);
        if (!valid) {
            //this is the contract. See AbstractPreAuthenticatedProcessingFilter#doAuthenticate
            LOG.warn("Missing required attribute(s): uid {}, schacHomeOrganization {}, givenName {}, familyName {}, email {}",
                    uid, schacHomeOrganization, givenName, familyName, email);
            return null;
        }
        Optional<User> optionalUser = userRepository.findUserByUid(uid);
        if (!optionalUser.isPresent()) {
            //If we would provision this email we would introduce a duplicate email
            userRepository.findUserByEmail(email).ifPresent(user -> {
                throw new MigrationDuplicateUserEmailException(email);
            });
        }
        return optionalUser.orElseGet(() -> provisionUser(uid, schacHomeOrganization, givenName, familyName, email, authenticatingAuthority));
    }

    private User provisionUser(String uid, String schacHomeOrganization, String givenName, String familyName, String email, String authenticatingAuthority) {
        User user = new User(uid, email, givenName, familyName, schacHomeOrganization, authenticatingAuthority);
        user.setNewUser(false);
        user = userRepository.save(user);

        LOG.info("Provision new User: uid {}, email {}, givenName {}, familyName {}, schacHomeOrganization {}, authenticatingAuthority {}",
                uid, email, givenName, familyName, schacHomeOrganization, authenticatingAuthority);

        if (oneginiEntityId.equalsIgnoreCase(user.getAuthenticatingAuthority())) {
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
                    new String(header.getBytes("ISO8859-1"), "UTF-8") : header;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }


}
