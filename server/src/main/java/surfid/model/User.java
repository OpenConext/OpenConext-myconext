package surfid.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

@NoArgsConstructor
@Getter
@Document(collection = "users")
public class User implements Serializable, UserDetails {

    @Id
    private String id;
    @NotNull
    private String email;
    private String givenName;
    private String familyName;
    private String password;

    private long updatedAt = System.currentTimeMillis() / 1000L;

    public User(@NotNull String email) {
        this(email, null, null);
    }

    public User(@NotNull String email, String givenName, String familyName) {
        this(email, givenName, familyName, null);
    }

    public User(@NotNull String email, String givenName, String familyName, String password) {
        this.email = email;
        this.givenName = givenName;
        this.familyName = familyName;
        this.password = password;
    }

    public void validate() {
        Assert.notNull(email, "GivenName is required");
        Assert.notNull(givenName, "GivenName is required");
        Assert.notNull(familyName, "GivenName is required");
    }

    public void encryptPassword(PasswordEncoder encoder) {
        if (StringUtils.hasText(password)) {
            this.password = encoder.encode(this.password);
        }
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("GUEST"));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    @Transient
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @Transient
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @Transient
    public boolean isEnabled() {
        return true;
    }

}
