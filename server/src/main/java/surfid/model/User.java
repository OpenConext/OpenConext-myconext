package surfid.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

@NoArgsConstructor
@Getter
@Document(collection = "users")
@EqualsAndHashCode(of = {"id"})
@ToString
public class User implements Serializable, UserDetails {

    @Id
    private String id;
    @NotNull
    private String email;
    @NotNull
    private String givenName;
    @NotNull
    private String familyName;

    private long updatedAt = System.currentTimeMillis() / 1000L;

    public User(String email, String givenName, String familyName) {
        this.email = email;
        this.givenName = givenName;
        this.familyName = familyName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("GUEST"));
    }

    @Override
    @Transient
    public String getPassword() {
        return null;
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
