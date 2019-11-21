package myconext.shibboleth;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;
import myconext.model.User;
import myconext.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

public class ShibbolethPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ShibbolethPreAuthenticatedProcessingFilter.class);

    public static final String SHIB_GIVEN_NAME = "Shib-givenName";
    public static final String SHIB_SUR_NAME = "Shib-surName";
    public static final String SHIB_EMAIL = "Shib-InetOrgPerson-mail";

    private final UserRepository userRepository;

    public ShibbolethPreAuthenticatedProcessingFilter(AuthenticationManager authenticationManager, UserRepository userRepository) {
        super();
        setAuthenticationManager(authenticationManager);
        this.userRepository = userRepository;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
        String givenName = getHeader(SHIB_GIVEN_NAME, request);
        String familyName = getHeader(SHIB_SUR_NAME, request);
        String email = getHeader(SHIB_EMAIL, request);

        if (StringUtils.isEmpty(givenName) || StringUtils.isEmpty(familyName) || StringUtils.isEmpty(email)) {
            //this is the contract. See AbstractPreAuthenticatedProcessingFilter#doAuthenticate
            LOG.warn("Missing required attribute(s): givenName {} familyName {} email {}", givenName, familyName, email);
            return null;
        }
        Optional<User> optionalUser = userRepository.findUserByEmail(email);
        return optionalUser.orElseGet(() -> provisionUser(givenName, familyName, email));
    }

    private User provisionUser(String givenName, String familyName, String email) {
        User user = new User(email, givenName, familyName);
        LOG.info("Provision new User: givenName {} familyName {} email {}", givenName, familyName, email);
        return userRepository.save(user);

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
