package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import myconext.manage.Manage;
import myconext.tiqr.SURFSecureID;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import tiqr.org.model.Registration;
import tiqr.org.model.RegistrationStatus;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Getter
public class UserResponse implements Serializable {

    private final String id;
    private final String email;
    private final String chosenName;
    private final String familyName;
    private final String givenName;
    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private final Date dateOfBirth;
    private final String displayName;
    private final boolean usePassword;
    private final boolean usePublicKey;
    private final boolean forgottenPassword;
    private final List<PublicKeyCredentials> publicKeyCredentials;
    private final List<LinkedAccount> linkedAccounts;
    private final List<ExternalLinkedAccount> externalLinkedAccounts;
    private final String schacHomeOrganization;
    private final String uid;
    private final boolean rememberMe;
    private final long created;
    private final long passwordUpdatedAt;
    private final Map<String, EduID> eduIdPerServiceProvider;
    private final List<String> loginOptions;
    private final Map<String, Object> registration = new HashMap<>();
    private final ControlCode controlCode;
    private final boolean serviceDeskMember;
    private final String preferredLanguage;

    public UserResponse(User user,
                        Map<String, EduID> eduIdPerServiceProvider,
                        Optional<Registration> optionalRegistration,
                        boolean rememberMe,
                        Manage manage,
                        List<VerifyIssuer> issuers) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.chosenName = user.getChosenName();
        this.givenName = user.getDerivedGivenName();
        this.familyName = user.getDerivedFamilyName();
        this.dateOfBirth = user.getDerivedDateOfBirth();
        this.displayName = this.givenName + " " + this.familyName;
        this.schacHomeOrganization = user.getSchacHomeOrganization();
        this.uid = user.getUid();
        this.usePassword = StringUtils.hasText(user.getPassword());
        this.publicKeyCredentials = user.getPublicKeyCredentials();
        this.linkedAccounts = user.getLinkedAccounts();
        if (!CollectionUtils.isEmpty(this.linkedAccounts)) {
            linkedAccounts.forEach(linkedAccount -> {
                Optional<IdentityProvider> optionalIdentityProvider = manage.findIdentityProviderByDomainName(linkedAccount.getSchacHomeOrganization());
                optionalIdentityProvider.ifPresent(identityProvider -> {
                    linkedAccount.setDisplayNameEn(identityProvider.getName());
                    linkedAccount.setDisplayNameNl(identityProvider.getNameNl());
                    linkedAccount.setInstitutionGuid(identityProvider.getInstitutionGuid());
                    linkedAccount.setLogoUrl(identityProvider.getLogoUrl());
                });
            });
        }
        this.externalLinkedAccounts = user.getExternalLinkedAccounts().stream()
                .filter(externalLinkedAccount -> !externalLinkedAccount.getIdpScoping().equals(IdpScoping.studielink) ||
                        !Verification.Ongeverifieerd.equals(externalLinkedAccount.getVerification()))
                .map(externalLinkedAccount -> externalLinkedAccount.logoReference(issuers))
                .collect(Collectors.toList());
        this.usePublicKey = !CollectionUtils.isEmpty(this.publicKeyCredentials);
        this.forgottenPassword = user.isForgottenPassword();
        this.rememberMe = rememberMe;
        this.created = user.getCreated();
        this.passwordUpdatedAt = user.getPasswordUpdatedAt();
        if (!CollectionUtils.isEmpty(eduIdPerServiceProvider)) {
            //Prevent HttpMessageNotWritableException: Could not write JSON: Null key for a Map not allowed in JSON
            eduIdPerServiceProvider.keySet().removeIf(Objects::isNull);
            eduIdPerServiceProvider.values().removeIf(Objects::isNull);
        }
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
        this.controlCode = user.getControlCode();
        this.serviceDeskMember = user.isServiceDeskMember();
        this.preferredLanguage = user.getPreferredLanguage();
    }
}
