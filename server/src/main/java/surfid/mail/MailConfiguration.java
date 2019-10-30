package surfid.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfiguration {

    @Value("${email.from}")
    private String emailFrom;

    @Value("${email.base-url}")
    private String baseUrl;

    @Autowired
    private JavaMailSender mailSender;

    @Bean
    @Profile({"!dev"})
    public MailBox mailSenderProd() {
        return new MailBox(mailSender, emailFrom, baseUrl);
    }

    @Bean
    @Profile({"dev"})
    @Primary
    public MailBox mailSenderDev() {
        return new MockMailBox(mailSender, emailFrom, baseUrl);
    }


}
