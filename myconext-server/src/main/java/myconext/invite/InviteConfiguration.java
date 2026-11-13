package myconext.invite;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class InviteConfiguration {

    @Bean
    public Invite invite(@Value("${invite.username}") String userName,
                         @Value("${invite.password}") String password,
                         @Value("${invite.base_url}") String baseUrl,
                         @Value("${invite.cache_minutes}") long cacheMinutes,
                         @Value("${invite.enabled}") boolean enabled) {
        return enabled ? new RemoteInvite(userName, password, baseUrl, Duration.ofMinutes(cacheMinutes)) :
                new MockInvite();
    }

}
