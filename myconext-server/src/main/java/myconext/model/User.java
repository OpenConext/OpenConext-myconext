package myconext.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import myconext.exceptions.WeakPasswordException;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
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

import static myconext.validation.PasswordStrength.strongEnough;

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
    private boolean newUser;

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
        this.newUser = true;
    }

    public void validate() {
        Assert.notNull(email, "Email is required");
        Assert.notNull(givenName, "GivenName is required");
        Assert.notNull(familyName, "FamilyName is required");
    }

    public void encryptPassword(String password, PasswordEncoder encoder) {
        if (StringUtils.hasText(password)) {
            if (!strongEnough(password)) {
                throw new WeakPasswordException();
            }
            this.password = encoder.encode(password);
        } else {
            this.password = null;
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

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public void clearPassword() {
        this.password = null;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

}
