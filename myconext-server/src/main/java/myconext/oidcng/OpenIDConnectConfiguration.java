package myconext.oidcng;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Map;

@Configuration
public class OpenIDConnectConfiguration {

    @Bean
    @Profile({"dev"})
    @Primary
    public OpenIDConnect openIDConnectMock(ObjectMapper objectMapper) throws IOException {
        List<Map<String, Object>> tokens = objectMapper.readValue(new ClassPathResource("oidcng/tokens.json").getInputStream(), new TypeReference<List<Map<String, Object>>>() {
        });
        return new OpenIDConnectMock(tokens);
    }

    @Bean
    @Profile({"!dev"})
    public OpenIDConnect openIDConnectRemote(@Value("${oidc-token-api.token-url}") URI oidcngUri,
                                       @Value("${oidc-token-api.user}") String user,
                                       @Value("${oidc-token-api.password}") String password) {
        return new OpenIDConnectRemote(oidcngUri, user, password);
    }
}
