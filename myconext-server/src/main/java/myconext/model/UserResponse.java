package myconext.model;

import lombok.Getter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tiqr.org.model.Registration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class UserResponse implements Serializable {

    private final String id;
    private final String email;
    private final String givenName;
    private final String familyName;
    private final boolean usePassword;
    private final boolean usePublicKey;
    private final boolean forgottenPassword;
    private final List<PublicKeyCredentials> publicKeyCredentials;
    private final List<LinkedAccount> linkedAccounts;
    private final String schacHomeOrganization;
    private final String uid;
    private final boolean rememberMe;
    private final long created;
    private final Map<String, EduID> eduIdPerServiceProvider;
    private final List<String> loginOptions;
    private final Map<String, Object> registration = new HashMap<>();

    public UserResponse(User user, Map<String, EduID> eduIdPerServiceProvider, Optional<Registration> optionalRegistration, boolean rememberMe) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.givenName = user.getGivenName();
        this.familyName = user.getFamilyName();
        this.schacHomeOrganization = user.getSchacHomeOrganization();
        this.uid = user.getUid();
        this.usePassword = StringUtils.hasText(user.getPassword());
        this.publicKeyCredentials = user.getPublicKeyCredentials();
        this.linkedAccounts = user.getLinkedAccounts();
        this.usePublicKey = !CollectionUtils.isEmpty(this.publicKeyCredentials);
        this.forgottenPassword = user.isForgottenPassword();
        this.rememberMe = rememberMe;
        this.created = user.getCreated();
        this.eduIdPerServiceProvider = eduIdPerServiceProvider;
        this.loginOptions = user.loginOptions();
        optionalRegistration.ifPresent(reg -> {
            registration.put("created", reg.getCreated().toEpochMilli());
            registration.put("status", reg.getStatus());
            registration.put("notificationType", reg.getNotificationType());
            registration.put("notificationAddress", reg.getNotificationAddress());
            registration.put("phoneVerified", user.getSurfSecureId().containsKey("phone-verified"));
            registration.put("recoveryCode", user.getSurfSecureId().containsKey("recovery-code"));
            registration.put("updated", reg.getUpdated().toEpochMilli());
        });
    }
}
