package surfid.model;

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

    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.givenName = user.getGivenName();
        this.familyName = user.getFamilyName();
        this.usePassword = StringUtils.hasText(user.getPassword());
    }
}
