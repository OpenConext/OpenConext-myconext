package myconext.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static myconext.security.SecurityConfiguration.InternalSecurityConfigurationAdapter.ROLE_GUEST;
import static myconext.security.SecurityConfiguration.InternalSecurityConfigurationAdapter.SERVICE_DESK;

@NoArgsConstructor
@Getter
@Document(collection = "external_users")
public class ExternalUser implements Serializable, UserDetails {

    private static final List<SimpleGrantedAuthority> GUEST_AUTHORITIES = List.of(new SimpleGrantedAuthority(ROLE_GUEST));
    private static final List<SimpleGrantedAuthority> SERVICE_DESK_AUTHORIES = Stream.of(ROLE_GUEST, SERVICE_DESK)
            .map(SimpleGrantedAuthority::new)
            .toList();

    @Id
    private String id;
    //Do not index the email here, this is already done in MongoMapping with custom strength (case-insensitive)
    @Setter
    private String email;
    @Setter
    private String givenName;
    @Setter
    private String familyName;
    @Indexed
    private String uid;
    private String schacHomeOrganization;
    @Setter
    private boolean newUser;
    private long created;
    @Setter
    private long lastLogin;

    @Setter
    private boolean serviceDeskMember;

    public ExternalUser(String uid, String email, String givenName, String familyName,
                        String schacHomeOrganization) {
        this.uid = uid;
        this.email = email;
        this.givenName = givenName;
        this.familyName = familyName;
        this.schacHomeOrganization = StringUtils.hasText(schacHomeOrganization) ? schacHomeOrganization.toLowerCase() : schacHomeOrganization;
        this.created = System.currentTimeMillis() / 1000L;
    }

    public void validate() {
        Assert.notNull(email, "Email is required");
        Assert.notNull(givenName, "GivenName is required");
        Assert.notNull(familyName, "FamilyName is required");
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (serviceDeskMember) {
            return SERVICE_DESK_AUTHORIES;
        }
        return GUEST_AUTHORITIES;
    }

    @Override
    public String getPassword() {
        return "";
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

    public String getEduPersonPrincipalName() {
        return uid + "@" + schacHomeOrganization;
    }

    public Map<String, Object> serviceDeskSummary() {
        return Map.of(
                "name", String.format("%s %s", this.getGivenName(), this.getFamilyName()),
                "email", this.email,
                "serviceDeskMember", this.serviceDeskMember
        );
    }

    public String getName() {
        return String.format("%s %s", givenName, familyName);
    }

}