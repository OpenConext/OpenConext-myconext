package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

@Getter
public class UserResponse {

    private String id;
    private String email;
    private String givenName;
    private String familyName;
    private boolean usePassword;
    private boolean usePublicKey;
    private String schacHomeOrganization;
    private String uid;
    private boolean rememberMe;
    private long created;

    public UserResponse(User user, boolean rememberMe) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.givenName = user.getGivenName();
        this.familyName = user.getFamilyName();
        this.schacHomeOrganization = user.getSchacHomeOrganization();
        this.uid = user.getUid();
        this.usePassword = StringUtils.hasText(user.getPassword());
        this.usePublicKey = StringUtils.hasText(user.getPublicKey());
        this.rememberMe = rememberMe;
        this.created = user.getCreated();
    }
}
