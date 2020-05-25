package myconext.oidc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

//@Service
public class AccountLinker {

    private final String authorizationUrl;
    private final String clientId;
    private final String redirectUri;
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpHeaders headers = new HttpHeaders();

    public AccountLinker(
            @Value("${oidc.client-id}") String clientId,
            @Value("${oidc.secret}") String clientSecret,
            @Value("${oidc.redirect-url}") String redirectUri,
            @Value("${oidc.authorization-url}") String authorizationUrl) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.authorizationUrl = authorizationUrl;
        this.headers.set("Accept", "application/json");
    }

    public ResponseEntity linkAccountRedirect() {
        Map<String, String> params = new HashMap<>();

        params.put("client_id", clientId);
        params.put("response_type", "code");
        params.put("scope", "openid");
        params.put("redirect_uri", redirectUri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(authorizationUrl);
        params.forEach(builder::queryParam);


        HttpEntity<Map> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, new HttpEntity(headers), Map.class);
        return null;
    }
}
