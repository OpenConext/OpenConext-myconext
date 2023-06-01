package myconext.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UserAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final long createdAt;

    public UserAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.createdAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
