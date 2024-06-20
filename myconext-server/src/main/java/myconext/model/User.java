package myconext.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import myconext.exceptions.WeakPasswordException;
import myconext.manage.Manage;
import myconext.remotecreation.NewExternalEduID;
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
    private List<ExternalLinkedAccount> externalLinkedAccounts = new ArrayList<>();
    @Setter
    private List<EduID> eduIDS = new ArrayList<>();

    private long created;
    private long updatedAt = System.currentTimeMillis() / 1000L;
    @Setter
    @Indexed
    private String trackingUuid;
    @Setter
    private long lastSeenAppNudge;

    public User(CreateInstitutionEduID createInstitutionEduID, Map<String, Object> userInfo) {
        this.email = createInstitutionEduID.getEmail();
        this.chosenName = (String) userInfo.get("given_name");
        this.givenName = (String) userInfo.get("given_name");
        this.familyName = (String) userInfo.get("family_name");
    }

    public User(String uid, String email, String chosenName, String givenName, String familyName, String schacHomeOrganization, String preferredLanguage,
                String serviceProviderEntityId, Manage manage) {
        this.uid = uid;
        this.email = email;
        this.chosenName = chosenName;
        this.givenName = givenName;
        this.familyName = familyName;
        this.schacHomeOrganization = schacHomeOrganization;
        this.preferredLanguage = preferredLanguage;

        this.computeEduIdForServiceProviderIfAbsent(serviceProviderEntityId, manage);
        this.newUser = true;
        this.created = System.currentTimeMillis() / 1000L;
        this.updatedAt = created;
    }

    public User(String uid, String email, String chosenName, String givenName, String familyName, String schacHomeOrganization, String preferredLanguage,
                IdentityProvider identityProvider, Manage manage) {
        this.uid = uid;
        this.email = email;
        this.chosenName = chosenName;
        this.givenName = givenName;
        this.familyName = familyName;
        this.schacHomeOrganization = schacHomeOrganization;
        this.preferredLanguage = preferredLanguage;

        this.computeEduIdForIdentityProviderProviderIfAbsent(identityProvider, manage);
        this.newUser = true;
        this.created = System.currentTimeMillis() / 1000L;
        this.updatedAt = created;
    }

    public void validate() {
        Assert.notNull(email, "Email is required");
        Assert.notNull(givenName, "GivenName is required");
        Assert.notNull(familyName, "FamilyName is required");
    }

    public void encryptPassword(String password, PasswordEncoder encoder) {
        if (!strongEnough(password)) {
            throw new WeakPasswordException();
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
        //Not likely and not desirable, but we don't interrupt the login flow for missing services
        ServiceProvider serviceProvider = manage.findServiceProviderByEntityId(entityId)
                .orElse(new ServiceProvider(new RemoteProvider(entityId, entityId, entityId, null, null), null));
        return doComputeEduIDIfAbsent(serviceProvider, manage);
    }

    @Transient
    public String computeEduIdForIdentityProviderProviderIfAbsent(IdentityProvider identityProvider, Manage manage) {
        ServiceProvider serviceProvider = new ServiceProvider(
                new RemoteProvider(null, identityProvider.getName(), identityProvider.getNameNl(), identityProvider.getInstitutionGuid(), identityProvider.getLogoUrl()),
                null
        );
        return doComputeEduIDIfAbsent(serviceProvider, manage);
    }

    private String doComputeEduIDIfAbsent(ServiceProvider serviceProvider, Manage manage) {
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
        //Let's be proactive and migrate the other eduID as well
        List<EduID> otherEduIDs = this.eduIDS.stream()
                .filter(eduID -> !eduID.getValue().equals(eduIDValue))
                //Only migrate old eduID's that have not been migrated already
                .filter(eduID -> eduID.getServices().isEmpty() && eduID.getServiceProviderEntityId() != null)
                .collect(Collectors.toList());
        otherEduIDs.forEach(eduID -> {
            String otherEntityId = eduID.getServiceProviderEntityId();
            ServiceProvider serviceProviderFromManage = manage.findServiceProviderByEntityId(otherEntityId)
                    .orElse(new ServiceProvider(new RemoteProvider(otherEntityId, otherEntityId, otherEntityId, null, null), null));
            eduID.updateServiceProvider(serviceProviderFromManage);
        });
        return eduIDValue;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_GUEST"));
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
        result.add(LoginOptions.MAGIC);
        return result.stream().map(LoginOptions::getValue).collect(Collectors.toList());
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

}
