package myconext.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import myconext.exceptions.WeakPasswordException;
import myconext.manage.Manage;
import myconext.remotecreation.NewExternalEduID;
import myconext.security.ServicesConfiguration;
import myconext.tiqr.SURFSecureID;
import myconext.verify.AttributeMapper;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static myconext.security.SecurityConfiguration.InternalSecurityConfigurationAdapter.ROLE_GUEST;
import static myconext.security.SecurityConfiguration.InternalSecurityConfigurationAdapter.SERVICE_DESK;
import static myconext.validation.PasswordStrength.strongEnough;

@NoArgsConstructor
@Getter
@Document(collection = "users")
public class User implements Serializable, UserDetails {

    @Id
    private String id;
    //Do not index the email here, this is already done in MongoMapping with a custom strength (case-insensitive)
    @Setter
    private String email;
    @Setter
    private String chosenName;
    @Setter
    private String givenName;
    @Setter
    private String familyName;
    @Setter
    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date dateOfBirth;
    @Indexed
    private String uid;
    private String schacHomeOrganization;
    private String password;
    @Setter
    private boolean newUser;
    @Setter
    private String preferredLanguage;
    @Setter
    private String webAuthnIdentifier;
    @Setter
    private String userHandle;
    @Setter
    private boolean forgottenPassword;
    @Setter
    @Indexed
    private String enrollmentVerificationKey;
    @Setter
    @Indexed
    private String createFromInstitutionKey;
    //Attributes and surfSecureId can't be final because of Jackson serialization (despite what your IDE tells tou)
    private Map<String, List<String>> attributes = new HashMap<>();
    private Map<String, Object> surfSecureId = new HashMap<>();

    @Setter
    private List<PublicKeyCredentials> publicKeyCredentials = new ArrayList<>();
    @Setter
    private List<LinkedAccount> linkedAccounts = new ArrayList<>();
    //Can't be final, despite what your IDE says
    @Setter
    private List<ExternalLinkedAccount> externalLinkedAccounts = new ArrayList<>();
    @Setter
    private List<EduID> eduIDS = new ArrayList<>();

    private long created;
    @Setter
    private long lastLogin;

    @Setter
    private boolean nudgeAppMailSend;
    @Setter
    @Indexed
    private String trackingUuid;
    private long lastSeenAppNudge;
    @Transient
    @JsonIgnore
    @Setter
    private boolean mobileAuthentication;

    @Setter
    private UserInactivity userInactivity;

    @Setter
    private boolean serviceDeskMember;

    @Setter
    private ControlCode controlCode;

    public User(CreateInstitutionEduID createInstitutionEduID, Map<String, Object> userInfo) {
        this.email = createInstitutionEduID.getEmail();
        this.chosenName = (String) userInfo.get("given_name");
        this.givenName = (String) userInfo.get("given_name");
        this.familyName = (String) userInfo.get("family_name");
    }

    public User(String uid, String email, String chosenName, String givenName, String familyName,
                String schacHomeOrganization, String preferredLanguage,
                String serviceProviderEntityId, Manage manage) {
        this.uid = uid;
        this.email = email;
        this.chosenName = chosenName;
        this.givenName = givenName;
        this.familyName = familyName;
        this.schacHomeOrganization = StringUtils.hasText(schacHomeOrganization) ? schacHomeOrganization.toLowerCase() : schacHomeOrganization;
        this.preferredLanguage = preferredLanguage;
        if (StringUtils.hasText(serviceProviderEntityId)) {
            this.computeEduIdForServiceProviderIfAbsent(serviceProviderEntityId, manage);
        }
        this.newUser = true;
        this.created = System.currentTimeMillis() / 1000L;
    }

    public User(String uid, String email, String chosenName, String givenName, String familyName,
                String schacHomeOrganization, String preferredLanguage,
                RemoteProvider remoteProvider, Manage manage) {
        this(uid, email, chosenName, givenName, familyName, schacHomeOrganization, preferredLanguage, (String) null,
                manage);
        this.computeEduIdForIdentityProviderProviderIfAbsent(remoteProvider, manage);
    }

    public void validate() {
        Assert.notNull(email, "Email is required");
        Assert.notNull(givenName, "GivenName is required");
        Assert.notNull(familyName, "FamilyName is required");
    }

    public void encryptPassword(String password, PasswordEncoder encoder) {
        if (!strongEnough(password)) {
            throw new WeakPasswordException("Weak password: " + password);
        }
        this.password = encoder.encode(password);
    }

    public void deletePassword() {
        this.password = null;
    }

    @Transient
    public void addPublicKeyCredential(PublicKeyCredentialDescriptor publicKeyCredentialDescriptor,
                                       ByteArray publicKeyCredential,
                                       String name) {
        this.publicKeyCredentials.add(new PublicKeyCredentials(
                publicKeyCredentialDescriptor,
                publicKeyCredential,
                name));
    }

    /**
     * Generate a new eduID for this Service Provider (based on the entityID and institutionGuid if present).
     * An eduID can be the same for multiple services if the institutionGuid's are identical (meaning the services are
     * coupled to one Identity Provider). If a Service Provider does not have an institutionGuid then there
     * is one unique eduID generated or existing eduID returned.
     *
     * @param entityId unique entityID of the Service Provider
     * @param manage   responsible for retrieving the Service Provider metadata information
     * @return the newly generated eduID or existing eduID
     */
    @Transient
    public String computeEduIdForServiceProviderIfAbsent(String entityId, Manage manage) {
        //Not likely and not desirable, but we don't interrupt the login flow for missing services or when manage is down
        ServiceProvider serviceProvider;
        try {
            serviceProvider = manage.findServiceProviderByEntityId(entityId)
                    .orElse(new ServiceProvider(new RemoteProvider(entityId, entityId, entityId, null, null), null));
        } catch (RuntimeException e) {
            serviceProvider = new ServiceProvider(new RemoteProvider(entityId, entityId, entityId, null, null), null);
        }
        return doComputeEduIDIfAbsent(serviceProvider, manage);
    }

    @Transient
    public String computeEduIdForIdentityProviderProviderIfAbsent(RemoteProvider remoteProvider, Manage manage) {
        //we want to pre-provision the eduID based on the institutional GUID, not the entityID
        remoteProvider.setEntityId(null);
        ServiceProvider serviceProvider = new ServiceProvider(remoteProvider, null);
        return doComputeEduIDIfAbsent(serviceProvider, manage);
    }

    private String doComputeEduIDIfAbsent(ServiceProvider serviceProvider, Manage manage) {
        this.lastLogin = System.currentTimeMillis();
        serviceProvider.setLastLogin(new Date());
        String institutionGuid = serviceProvider.getInstitutionGuid();
        String entityId = serviceProvider.getEntityId();
        //We need to be backward compatible, so we need to check both obsolete properties and the services
        Optional<EduID> optionalExistingEduID = this.eduIDS.stream()
                .filter(eduID -> {
                    //Ensure we don't match institutionGUID's that are both null
                    boolean matchByInstitutionGUID = StringUtils.hasText(institutionGuid) &&
                            (institutionGuid.equals(eduID.getServiceInstutionGuid()) ||
                                    eduID.getServices().stream().anyMatch(sp -> institutionGuid.equals(sp.getInstitutionGuid())));
                    //Ensure we don't match entityId's that are both null
                    boolean matchByEntityId = StringUtils.hasText(entityId) &&
                            (entityId.equals(eduID.getServiceProviderEntityId()) ||
                                    eduID.getServices().stream().anyMatch(sp -> entityId.equals(sp.getEntityId())));
                    return matchByInstitutionGUID || matchByEntityId;
                }).findFirst();
        //If there is an existing eduID then we add or update the service for this eduID, otherwise add new one
        String eduIDValue = optionalExistingEduID.map(
                eduId -> eduId.updateServiceProvider(serviceProvider).getValue()
        ).orElseGet(() -> {
            EduID eduID = new EduID(UUID.randomUUID().toString(), serviceProvider);
            this.eduIDS.add(eduID);
            return eduID.getValue();
        });
        //Let's be proactive and migrate the other eduID values as well
        List<EduID> otherEduIDs = this.eduIDS.stream()
                .filter(eduID -> !eduID.getValue().equals(eduIDValue))
                //Only migrate old eduID's that have not been migrated already
                .filter(eduID -> eduID.getServices().isEmpty() && eduID.getServiceProviderEntityId() != null)
                .toList();
        otherEduIDs.forEach(eduID -> {
            String otherEntityId = eduID.getServiceProviderEntityId();
            try {
                manage.findServiceProviderByEntityId(otherEntityId)
                        .ifPresent(eduID::updateServiceProvider);
            } catch (RuntimeException e) {
                // not to be helped
            }
        });
        return eduIDValue;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (serviceDeskMember) {
            return Stream.of(ROLE_GUEST, SERVICE_DESK)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority(ROLE_GUEST));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @Transient
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Transient
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Transient
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Transient
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Transient
    @JsonIgnore
    public List<LinkedAccount> linkedAccountsSorted() {
        return this.linkedAccounts.stream()
                .sorted(Comparator.comparing(LinkedAccount::getExpiresAt).reversed()).collect(Collectors.toList());
    }

    @Transient
    @JsonIgnore
    public List<String> allEduPersonAffiliations() {
        return linkedAccounts.stream()
                .map(LinkedAccount::getEduPersonAffiliations)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    @Transient
    @JsonIgnore
    public List<String> loginOptions() {
        List<LoginOptions> result = new ArrayList<>();
        //Order by priority
        if (!CollectionUtils.isEmpty(this.surfSecureId) && (
                this.surfSecureId.containsKey(SURFSecureID.PHONE_VERIFIED) ||
                        this.surfSecureId.containsKey(SURFSecureID.RECOVERY_CODE))) {
            result.add(LoginOptions.APP);
        }
        if (!CollectionUtils.isEmpty(this.publicKeyCredentials)) {
            result.add(LoginOptions.FIDO);
        }
        if (StringUtils.hasText(this.password)) {
            result.add(LoginOptions.PASSWORD);
        }
        result.add(LoginOptions.CODE);
        return result.stream().map(LoginOptions::getValue).collect(Collectors.toList());
    }

    @Transient
    @JsonIgnore
    public Map<String, EduID> convertEduIdPerServiceProvider(ServicesConfiguration servicesConfiguration) {
        //We need to be backward compatible, but also deal with new many services refactor. Key of map may not be null
        Map<String, EduID> result = new HashMap<>();
        List<String> hideInOverview = servicesConfiguration.getHideInOverview();
        this.getEduIDS().forEach(eduID -> {
            if (CollectionUtils.isEmpty(eduID.getServices()) && StringUtils.hasText(eduID.getServiceProviderEntityId())
                    && !hideInOverview.contains(eduID.getServiceProviderEntityId())) {
                result.put(eduID.getServiceProviderEntityId(), eduID);
            } else {
                eduID.getServices().stream().filter(service -> !hideInOverview.contains(service.getEntityId()))
                        .forEach(service -> {
                            String entityId = service.getEntityId();
                            String key = StringUtils.hasText(entityId) ? entityId : service.getInstitutionGuid();
                            //need to make copy otherwise the reference is the same and for mobile authentication we override the properties
                            result.put(key, eduID.copy(key));
                        });
            }
        });
        //The mobile API expects the old format. For all keys we fill the obsolete attributes of an eduID
        if (this.isMobileAuthentication()) {
            result.forEach((entityId, eduID) -> {
                if (!CollectionUtils.isEmpty(eduID.getServices())) {
                    ServiceProvider serviceProvider = eduID.getServices().stream()
                            .filter(sp -> entityId.equals(sp.getEntityId())).findFirst()
                            .orElse(eduID.getServices().get(0));
                    eduID.backwardCompatibleTransformation(serviceProvider);
                }
            });
        }
        return result;
    }

    @Transient
    @JsonIgnore
    public void updateWithExternalEduID(NewExternalEduID externalEduID) {
        //Only update attributes when there is no validated account
        if (CollectionUtils.isEmpty(this.externalLinkedAccounts) && CollectionUtils.isEmpty(this.linkedAccounts)) {
            this.givenName = externalEduID.getFirstName();
            String lastNamePrefix = externalEduID.getLastNamePrefix();
            this.familyName = StringUtils.hasText(lastNamePrefix) ? String.format("%s %s", lastNamePrefix, externalEduID.getLastName()) : externalEduID.getLastName();
            this.dateOfBirth = AttributeMapper.parseDate(externalEduID.getDateOfBirth());
        }
    }

    public String getEduPersonPrincipalName() {
        return uid + "@" + schacHomeOrganization;
    }


    @Transient
    @JsonIgnore
    public boolean reconcileLinkedAccounts() {
        List<ProvisionedLinkedAccount> provisionedLinkedAccounts = new ArrayList<>();
        provisionedLinkedAccounts.addAll(this.linkedAccounts);
        provisionedLinkedAccounts.addAll(this.externalLinkedAccounts);
        boolean preferredInstitution = provisionedLinkedAccounts.stream().anyMatch(ProvisionedLinkedAccount::isPreferred);
        if (!preferredInstitution) {
            Optional<ProvisionedLinkedAccount> first = provisionedLinkedAccounts.stream()
                    .max(Comparator.comparing(ProvisionedLinkedAccount::getCreatedAt));
            first.ifPresent(provisionedLinkedAccount -> provisionedLinkedAccount.setPreferred(true));
            return first.isPresent();
        }
        return false;
    }

    //Prevent @Getter lombok generation as we don't want any logic on this value anywhere else but here
    private long getLastSeenAppNudge() {
        return this.lastSeenAppNudge;
    }

    @Transient
    @JsonIgnore
    public boolean nudgeToApp(int nudgeAppDays, int nudgeAppDelayDays) {
        int oneDayMillis = 1000 * 60 * 60 * 24;
        long nowMillis = System.currentTimeMillis();
        //this.created are seconds and not millis, will not change this for backward compatibility in Mobile GUI
        long createdMillis = this.created * 1000;
        long nudgeAppMillis = (long) nudgeAppDays * oneDayMillis;

        // First 24 hours we don't show the app nudge
        boolean createdMoreThenOneDayAgo = createdMillis < (nowMillis - oneDayMillis);
        // First subsequent login is before nudgeAppDays
        boolean secondLoginBeforeNudgeAppDays = this.lastSeenAppNudge == 0L &&
                (nowMillis - createdMillis) < nudgeAppMillis;
        // Nudge app delay time has reached
        boolean delayReached = this.lastSeenAppNudge != 0L &&
                (nowMillis - this.lastSeenAppNudge) > ((long) oneDayMillis * nudgeAppDelayDays);

        // Corner case, user has postponed the second login for long time, but is now an active user
        boolean cornerCase = createdMoreThenOneDayAgo && this.lastSeenAppNudge == 0L
                && (nowMillis - this.lastLogin) < nudgeAppMillis;

        boolean decision = createdMoreThenOneDayAgo && (secondLoginBeforeNudgeAppDays || delayReached || cornerCase);
        if (decision) {
            this.lastSeenAppNudge = nowMillis;
        }
        return decision;
    }

    public Date getDerivedDateOfBirth() {
        return CollectionUtils.isEmpty(this.externalLinkedAccounts) ? null : this.externalLinkedAccounts.get(0).getDateOfBirth();
    }

    public String getDerivedGivenName() {
        return this.getDerivedName(this.givenName, ProvisionedLinkedAccount::getGivenName);
    }

    public String getDerivedFamilyName() {
        return this.getDerivedName(this.familyName, ProvisionedLinkedAccount::getFamilyName);
    }

    private String getDerivedName(String fallback, ProvisionedNameProvider provisionedNameProvider) {
        if (CollectionUtils.isEmpty(this.linkedAccounts) && CollectionUtils.isEmpty(this.externalLinkedAccounts)) {
            return fallback;
        }
        List<ProvisionedLinkedAccount> provisionedLinkedAccounts = new ArrayList<>();
        provisionedLinkedAccounts.addAll(this.linkedAccounts);
        provisionedLinkedAccounts.addAll(this.externalLinkedAccounts);
        return provisionedLinkedAccounts.stream()
                .filter(ProvisionedLinkedAccount::isPreferred)
                .findFirst()
                .map(provisionedNameProvider::derivedName)
                .or(() -> provisionedLinkedAccounts.stream()
                        .filter(provisionedLinkedAccount -> provisionedLinkedAccount.getCreatedAt() != null)
                        .max(Comparator.comparing(ProvisionedLinkedAccount::getCreatedAt))
                        .stream().findFirst().map(provisionedNameProvider::derivedName))
                .orElse(fallback);
    }

    public Map<String, Object> serviceDeskSummary() {
        return Map.of(
                "name", String.format("%s %s", this.getDerivedGivenName(), this.getDerivedFamilyName()),
                "email", this.email,
                "serviceDeskMember", this.serviceDeskMember
        );
    }

    private interface ProvisionedNameProvider {

        String derivedName(ProvisionedLinkedAccount provisionedLinkedAccount);

    }

}