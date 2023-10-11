package myconext.manage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;

import java.util.Arrays;

@Configuration
public class ManageServiceProviderConfiguration {

    @Bean
    public ServiceProviderResolver serviceProviderResolver(@Value("${manage.username}") String userName,
                                                           @Value("${manage.password}") String password,
                                                           @Value("${manage.base_url}") String baseUrl,
                                                           @Value("${manage.enabled}") boolean enabled,
                                                           @Value("${manage.enabled}")  Resource resource,
                                                           ObjectMapper objectMapper,
                                                           Environment environment) {
        String[] activeProfiles = environment.getActiveProfiles();
        //We can't use the @Bean(profile) annotation as we need more flexibility
        if (activeProfiles.length > 0) {
            return new MockServiceProviderResolver();
        }
        return enabled ? new ManageServiceProviderResolver(userName, password, baseUrl) :
                new ResourceServiceProviderResolver(resource, objectMapper) ;
    }

}
