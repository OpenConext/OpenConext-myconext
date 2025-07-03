package myconext.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.IOException;

@Configuration
public class SMSConfiguration {

    @Bean
    public SMSService smsService(Environment environment, @Value("${sms.url}") String url,
                                 @Value("${sms.route}") String route,
                                 @Value("${sms.bearer}") String bearer) {
        return environment.getActiveProfiles().length == 0 ?
                new SMSServiceImpl(url, bearer, route) : new SMSServiceMock(environment);
    }

}
