package myconext.manage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
public class ManageServiceProviderConfiguration {

    @Bean
    public ServiceProviderResolver serviceProviderResolver(@Value("${manage.username}") String userName,
                                                           @Value("${manage.password}") String password,
                                                           @Value("${manage.base_url}") String baseUrl,
                                                           Environment environment) {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            Arrays.stream(Thread.currentThread().getStackTrace()).forEach(
                    System.out::println
            );
        }
        return activeProfiles.length > 0 ? new MockServiceProviderResolver() :
                new ManageServiceProviderResolver(userName, password, baseUrl);
    }

}
