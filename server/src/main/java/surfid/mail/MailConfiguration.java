package surfid.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfiguration {

    @Value("${email.from}")
    private String emailFrom;

    @Value("${email.magic-link-url}")
    private String magicLinkUrl;

    @Value("${email.my-surfconext-url}")
    private String mySURFconextURL;

    @Autowired
    private JavaMailSender mailSender;

    @Bean
    @Profile({"!dev"})
    public MailBox mailSenderProd() {
        return new MailBox(mailSender, emailFrom, magicLinkUrl, mySURFconextURL);
    }

    @Bean
    @Profile({"dev", "test"})
    @Primary
    public MailBox mailSenderDev(Environment environment) {
        return new MockMailBox(mailSender, emailFrom, magicLinkUrl, mySURFconextURL, environment);
    }


}
