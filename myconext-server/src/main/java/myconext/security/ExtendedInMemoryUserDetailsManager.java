package myconext.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;

public class ExtendedInMemoryUserDetailsManager implements UserDetailsService {

    private final String NOOP_PREFIX = "{noop}";
    private final Map<String, RemoteUser> users;

    public ExtendedInMemoryUserDetailsManager(List<RemoteUser> users) {
        List<RemoteUser> wrongConfiguredRemoteUsers = users.stream()
                .filter(remoteUser -> remoteUser.getPassword().contains(NOOP_PREFIX))
                .collect(toList());
        if (!wrongConfiguredRemoteUsers.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Remote users found with {noop} password. This is a configuration mistake. Check: %s",
                            wrongConfiguredRemoteUsers.stream().map(RemoteUser::getUsername)
                                    .collect(Collectors.joining(","))));
        }
        this.users = users.stream()
                .collect(Collectors.toMap(UserDetails::getUsername, identity()));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        RemoteUser remoteUser = users.get(username);
        if (remoteUser == null) {
            return null;
        }
        RemoteUser remoteUserClone = new RemoteUser(remoteUser);
        remoteUserClone.setPassword(NOOP_PREFIX.concat(remoteUserClone.getPassword()));
        return remoteUserClone;
    }
}
