package myconext.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ManageConfiguration {

    @Bean
    public Manage manage(@Value("${manage.username}") String userName,
                         @Value("${manage.password}") String password,
                         @Value("${manage.base_url}") String baseUrl,
                         @Value("${manage.enabled}") boolean enabled,
                         ObjectMapper objectMapper) {
        return enabled ? new RemoteManage(userName, password, baseUrl) :
                new MockManage(objectMapper);
    }

}
