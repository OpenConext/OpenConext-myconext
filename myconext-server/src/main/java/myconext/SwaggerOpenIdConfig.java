package myconext;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.ForwardedHeaderFilter;

import java.util.Map;

@Configuration
@OpenAPIDefinition
public class SwaggerOpenIdConfig {

    public static final String OPEN_ID_SCHEME_NAME = "openId";
    public static final String BASIC_AUTHENTICATION_SCHEME_NAME = "basic";

    @Bean
    OpenAPI customOpenApi(@Value("${eduid_api.base_url}") String baseUrl) {
        SecurityScheme openIDSecurityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows()
                        .authorizationCode(new OAuthFlow()
                                .scopes(new Scopes()
                                        .addString("openid", "OpenID Connect")
                                        .addString("eduid.nl/eppn", "Read linked eppn / schac_home_organization")
                                        .addString("eduid.nl/eduid", "Read the eduID value")
                                        .addString("eduid.nl/links", "Read linked accounts")
                                        .addString("eduid.nl/mobile", "Mobile app API access"))));

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

    /*
     * Fetching the OIDC discovery document is deferred to first use of the Swagger UI / api-docs
     * endpoint, instead of happening eagerly while the OpenAPI bean is constructed at application
     * startup. This avoids a hard dependency on the (external) discovery endpoint being reachable
     * every time the application context is started, e.g. in tests or when SURFconext is unreachable.
     */
    @Bean
    GlobalOpenApiCustomizer openIdConnectDiscoveryCustomizer(@Value("${eduid_api.oidcng_discovery_url}") String discoveryURL) {
        return openApi -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> discoveryDocument = new RestTemplate().getForObject(discoveryURL, Map.class);
            String authorizationEndpoint = (String) discoveryDocument.get("authorization_endpoint");
            String tokenEndpoint = (String) discoveryDocument.get("token_endpoint");

            OAuthFlow authorizationCodeFlow = openApi.getComponents()
                    .getSecuritySchemes()
                    .get(OPEN_ID_SCHEME_NAME)
                    .getFlows()
                    .getAuthorizationCode();
            authorizationCodeFlow.authorizationUrl(authorizationEndpoint);
            authorizationCodeFlow.tokenUrl(tokenEndpoint);
        };
    }

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}