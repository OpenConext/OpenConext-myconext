package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
@NoArgsConstructor
public class UserResponse {

    private String id;
    private String email;
    private String givenName;
    private String familyName;
    private boolean usePassword;
    private boolean rememberMe;

    public UserResponse(User user, boolean rememberMe) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.givenName = user.getGivenName();
        this.familyName = user.getFamilyName();
        this.usePassword = StringUtils.hasText(user.getPassword());
        this.rememberMe = rememberMe;
    }
}
