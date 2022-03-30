package myconext.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import myconext.exceptions.WeakPasswordException;
import myconext.manage.ServiceProviderResolver;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
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

    private String email;
    private String givenName;
    private String familyName;
    private String uid;
    private String schacHomeOrganization;
    private String password;
    private boolean newUser;
    private String preferredLanguage;
    private String webAuthnIdentifier;
    private String userHandle;
    private boolean forgottenPassword;

    private Map<String, List<String>> attributes = new HashMap<>();
    private Map<String, String> surfSecureId = new HashMap<>();

    private List<PublicKeyCredentials> publicKeyCredentials = new ArrayList<>();
    private List<LinkedAccount> linkedAccounts = new ArrayList<>();
    private List<EduID> eduIDS = new ArrayList<>();

    private long created;
    private long updatedAt = System.currentTimeMillis() / 1000L;
    private String trackingUuid;
    private long lastSeenAppNudge;


    public User(String uid, String email, String givenName, String familyName, String schacHomeOrganization, String preferredLanguage,
                String serviceProviderEntityId, ServiceProviderResolver serviceProviderResolver) {
        this.uid = uid;
        this.email = email;
        this.givenName = givenName;
        this.familyName = familyName;
        this.schacHomeOrganization = schacHomeOrganization;
        this.preferredLanguage = preferredLanguage;

        this.computeEduIdForServiceProviderIfAbsent(serviceProviderEntityId, serviceProviderResolver);
        this.newUser = true;
        this.created = System.currentTimeMillis() / 1000L;
        this.updatedAt = created;
        this.trackingUuid = UUID.randomUUID().toString();
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

    @Transient
    public void addPublicKeyCredential(PublicKeyCredentialDescriptor publicKeyCredentialDescriptor,
                                       ByteArray publicKeyCredential,
                                       String name) {
        this.publicKeyCredentials.add(new PublicKeyCredentials(
                publicKeyCredentialDescriptor,
                publicKeyCredential,
                name));
    }

    private boolean eduIDEquals(EduID eduID, Optional<ServiceProvider> optionalServiceProvider, String serviceProviderEntityId) {
        if (eduID.getServiceProviderEntityId().equalsIgnoreCase(serviceProviderEntityId)) {
            return true;
        }
        if (optionalServiceProvider.isPresent()) {
            ServiceProvider serviceProvider = optionalServiceProvider.get();
            if (StringUtils.hasText(serviceProvider.getInstitutionGuid())) {
                return serviceProvider.getInstitutionGuid().equalsIgnoreCase(eduID.getServiceInstutionGuid());
            }
        }
        return false;
    }

    @Transient
    public String computeEduIdForServiceProviderIfAbsent(String serviceProviderEntityId, ServiceProviderResolver serviceProviderResolver) {
        Optional<ServiceProvider> optionalServiceProvider = serviceProviderResolver.resolve(serviceProviderEntityId);
        Optional<EduID> optionalEduID = this.eduIDS.stream()
                .filter(eduID -> this.eduIDEquals(eduID, optionalServiceProvider, serviceProviderEntityId))
                .findFirst();
        if (optionalEduID.isPresent()) {
            EduID eduID = optionalEduID.get();
            optionalServiceProvider.ifPresent(eduID::updateServiceProvider);
            return eduID.getValue();
        } else {
            EduID eduID = new EduID(UUID.randomUUID().toString(), serviceProviderEntityId, optionalServiceProvider);
            this.eduIDS.add(eduID);
            return eduID.getValue();
        }
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
        return linkedAccounts.stream().map(LinkedAccount::getEduPersonAffiliations).flatMap(Collection::stream).collect(Collectors.toList());
    }

    @Transient
    @JsonIgnore
    public boolean nudgeToUseApp() {
        return lastSeenAppNudge < (System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7);
    }

    @Transient
    @JsonIgnore
    public List<String> loginOptions() {
        List<LoginOptions> result = new ArrayList<>();
        //Order by priority
        if (!CollectionUtils.isEmpty(this.surfSecureId)) {
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

    public String getEduPersonPrincipalName() {
        return uid + "@" + schacHomeOrganization;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public void setWebAuthnIdentifier(String webAuthnIdentifier) {
        this.webAuthnIdentifier = webAuthnIdentifier;
    }

    public void setUserHandle(String userHandle) {
        this.userHandle = userHandle;
    }

    public void setLinkedAccounts(List<LinkedAccount> linkedAccounts) {
        this.linkedAccounts = linkedAccounts;
    }

    public void setEduIDS(List<EduID> eduIDS) {
        this.eduIDS = eduIDS;
    }

    public void setPublicKeyCredentials(List<PublicKeyCredentials> publicKeyCredentials) {
        this.publicKeyCredentials = publicKeyCredentials;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setForgottenPassword(boolean forgottenPassword) {
        this.forgottenPassword = forgottenPassword;
    }

    public void setTrackingUuid(String trackingUuid) {
        this.trackingUuid = trackingUuid;
    }

    public void setLastSeenAppNudge(long lastSeenAppNudge) {
        this.lastSeenAppNudge = lastSeenAppNudge;
    }
}
