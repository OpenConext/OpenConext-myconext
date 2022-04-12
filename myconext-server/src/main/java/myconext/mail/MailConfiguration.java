package myconext.mail;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.repository.EmailsSendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfiguration {

    @Value("${email.from}")
    private String emailFrom;

    @Value("${email.magic-link-url}")
    private String magicLinkUrl;

    @Value("${email.my-surfconext-url}")
    private String mySURFconextURL;

    @Value("${email.mail-templates-directory}")
    private Resource mailTemplatesDirectory;

    @Value("${email_spam_threshold_seconds}")
    private long emailSpamThresholdSeconds;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmailsSendRepository emailsSendRepository;

    @Bean
    @Profile({"!dev"})
    public MailBox mailSenderProd() throws IOException {
        return new MailBox(mailSender, emailFrom, magicLinkUrl, mySURFconextURL, objectMapper, mailTemplatesDirectory,
                emailsSendRepository, emailSpamThresholdSeconds);
    }

    @Bean
    @Profile({"dev", "test", "shib"})
    @Primary
    public MailBox mailSenderDev(Environment environment) throws IOException {
        return new MockMailBox(mailSender, emailFrom, magicLinkUrl, mySURFconextURL, objectMapper, mailTemplatesDirectory, emailsSendRepository, environment);
    }


}
