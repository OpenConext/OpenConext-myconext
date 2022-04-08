package myconext.sms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
public class SMSConfiguration {

    @Bean
    @Profile("!dev")
    public SMSService smsService(@Value("${sms.url}") String url, @Value("${sms.bearer}") String bearer) {
        return new SMSServiceImpl(url, bearer);
    }

    @Bean
    @Profile("dev")
    public SMSService smsServiceMock() throws IOException {
        return new SMSServiceMock();
    }

}
