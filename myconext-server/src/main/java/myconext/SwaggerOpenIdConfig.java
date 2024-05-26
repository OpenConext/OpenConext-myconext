package myconext;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
@OpenAPIDefinition
public class SwaggerOpenIdConfig {

    public static final String OPEN_ID_SCHEME_NAME = "openId";
    public static final String BASIC_AUTHENTICATION_SCHEME_NAME = "basic";

    @Bean
    OpenAPI customOpenApi(@Value("${eduid_api.oidcng_discovery_url}") String discoveryURL,
                          @Value("${eduid_api.base_url}") String baseUrl) {
        SecurityScheme openIDSecurityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OPENIDCONNECT)
                .openIdConnectUrl(discoveryURL);

        SecurityScheme basicAuthenticationSecurityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic");

        Components components = new Components()
                .addSecuritySchemes(OPEN_ID_SCHEME_NAME, openIDSecurityScheme)
                .addSecuritySchemes(BASIC_AUTHENTICATION_SCHEME_NAME, basicAuthenticationSecurityScheme);

        OpenAPI openAPI = new OpenAPI()
                .info(new Info().description("eduID app API endpoints").title("eduID app API"))
                .addServersItem(new Server().url(baseUrl));

        openAPI.components(components)
                .addSecurityItem(new SecurityRequirement().addList(OPEN_ID_SCHEME_NAME))
                .addSecurityItem(new SecurityRequirement().addList(BASIC_AUTHENTICATION_SCHEME_NAME));
        return openAPI;
    }

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}