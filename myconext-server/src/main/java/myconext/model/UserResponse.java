package myconext.model;

import lombok.Getter;
import myconext.cron.IdPMetaDataResolver;
import myconext.cron.IdentityProvider;
import myconext.tiqr.SURFSecureID;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tiqr.org.model.Registration;
import tiqr.org.model.RegistrationStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
public class UserResponse implements Serializable {

    private final String id;
    private final String email;
    private final String callName;
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

    public UserResponse(User user,
                        Map<String, EduID> eduIdPerServiceProvider,
                        Optional<Registration> optionalRegistration,
                        boolean rememberMe,
                        IdPMetaDataResolver idPMetaDataResolver) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.callName = user.getChosenName();
        this.givenName = user.getGivenName();
        this.familyName = user.getFamilyName();
        this.schacHomeOrganization = user.getSchacHomeOrganization();
        this.uid = user.getUid();
        this.usePassword = StringUtils.hasText(user.getPassword());
        this.publicKeyCredentials = user.getPublicKeyCredentials();
        this.linkedAccounts = user.getLinkedAccounts();
        if (!CollectionUtils.isEmpty(this.linkedAccounts)) {
            linkedAccounts.forEach(linkedAccount -> {
                Optional<IdentityProvider> optionalIdentityProvider = idPMetaDataResolver.getIdentityProvider(linkedAccount.getSchacHomeOrganization());
                optionalIdentityProvider.ifPresent(identityProvider -> {
                    linkedAccount.setDisplayNameEn(identityProvider.getDisplayNameEn());
                    linkedAccount.setDisplayNameNl(identityProvider.getDisplayNameNl());
                    linkedAccount.setLogoUrl(identityProvider.getLogoUrl());
                });
            });
        }
        this.usePublicKey = !CollectionUtils.isEmpty(this.publicKeyCredentials);
        this.forgottenPassword = user.isForgottenPassword();
        this.rememberMe = rememberMe;
        this.created = user.getCreated();
        this.eduIdPerServiceProvider = eduIdPerServiceProvider;
        this.loginOptions = user.loginOptions();
        optionalRegistration.ifPresent(reg -> {
            if (!RegistrationStatus.FINALIZED.equals(reg.getStatus())) {
                //Only finalized registrations are returned
                return;
            }
            Map<String, Object> surfSecureId = user.getSurfSecureId();
            boolean phoneVerified = surfSecureId.containsKey(SURFSecureID.PHONE_VERIFIED);

            registration.put("created", reg.getCreated().toEpochMilli());
            registration.put("status", reg.getStatus());
            registration.put("notificationType", reg.getNotificationType());
            registration.put("notificationAddress", reg.getNotificationAddress());
            registration.put("phoneVerified", phoneVerified);
            registration.put("recoveryCode", surfSecureId.containsKey(SURFSecureID.RECOVERY_CODE));
            if (phoneVerified) {
                String phoneNumber = (String) surfSecureId.get(SURFSecureID.PHONE_NUMBER);
                registration.put("phoneNumber", phoneNumber.substring(phoneNumber.length() - 3));
            }
            registration.put("updated", reg.getUpdated().toEpochMilli());
        });
    }
}
