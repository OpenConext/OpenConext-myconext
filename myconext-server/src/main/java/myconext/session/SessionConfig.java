package myconext.session;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import myconext.model.EduID;
import myconext.model.LinkedAccount;
import myconext.model.PublicKeyCredentials;
import myconext.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.saml.saml2.authentication.Assertion;
import org.springframework.session.data.mongo.config.annotation.web.http.EnableMongoHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

@Configuration
@EnableMongoHttpSession
public class SessionConfig extends AbstractHttpSessionApplicationInitializer {

    @Bean(name = "jsonMapper")
    @Primary
    public ObjectMapper jsonMapper() {
        return new ObjectMapper()
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .addMixIn(Assertion.class, AssertionMixin.class)
                .addMixIn(HashSet.class, HashSetMixin.class)
                .addMixIn(User.class, UserMixin.class)
                .addMixIn(LinkedAccount.class, LinkedAccountMixin.class)
                .addMixIn(EduID.class, EduIDMixin.class)
                .addMixIn(PublicKeyCredentials.class, PublicKeyCredentialsMixin.class)
                .addMixIn(LinkedHashMap.class, LinkedHashMapMixin.class)
                .addMixIn(HashMap.class, HashMapMixin.class);

//                .registerModule(new CoreJackson2Module());
    }

    @Bean
    CookieSerializer cookieSerializer(@Value("${secure_cookie}") boolean secureCookie) {
        DefaultCookieSerializer defaultCookieSerializer = new DefaultCookieSerializer();
        defaultCookieSerializer.setSameSite(secureCookie ? "None" : "Lax");
        defaultCookieSerializer.setUseSecureCookie(secureCookie);
        return defaultCookieSerializer;
    }

    private static class AssertionMixin {
    }

    private static class HashSetMixin {
    }

    private static class UserMixin {
    }

    private static class LinkedAccountMixin {
    }

    private static class EduIDMixin {
    }

    private static class PublicKeyCredentialsMixin {
    }

    private static class LinkedHashMapMixin {
    }

    private static class HashMapMixin {
    }
}
