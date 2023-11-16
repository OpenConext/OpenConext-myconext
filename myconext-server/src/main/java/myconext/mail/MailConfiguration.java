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
import org.springframework.core.env.Profiles;
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

    @Value("${email.idp-surfconext-url}")
    private String loginSURFconextURL;

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

    @Autowired
    private Environment environment;

    @Bean
    public MailBox mailSenderProd() throws IOException {
        if (environment.acceptsProfiles(Profiles.of("dev", "test", "shib"))) {
            return new MockMailBox(mailSender, emailFrom, magicLinkUrl, mySURFconextURL, loginSURFconextURL, objectMapper, mailTemplatesDirectory, emailsSendRepository, environment);
        }
        return new MailBox(mailSender, emailFrom, magicLinkUrl, mySURFconextURL, loginSURFconextURL, objectMapper, mailTemplatesDirectory,
                emailsSendRepository, emailSpamThresholdSeconds);
    }
}
