package myconext.security;

import myconext.exceptions.UserNotFoundException;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserAuthentication {

    UserRepository getUserRepository();

    @SuppressWarnings("unchecked")
    default User userFromAuthentication(Authentication authentication) {
        User user;
        boolean mobileAuthentication = authentication instanceof BearerTokenAuthentication;
        if (mobileAuthentication) {
            Map<String, Object> tokenAttributes = ((BearerTokenAuthentication) authentication).getTokenAttributes();
            String uid = ((List<String>) tokenAttributes.get("uids")).get(0);
            user = getUserRepository().findUserByUid(uid).orElseThrow(() -> new UserNotFoundException(uid));
        } else {
            String userId = ((User) authentication.getPrincipal()).getId();
            user = getUserRepository().findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        }
        user.setMobileAuthentication(mobileAuthentication);
        return user;
    }

    default boolean isMobileRequest(Authentication authentication) {
        return authentication instanceof BearerTokenAuthentication;
    }


}
