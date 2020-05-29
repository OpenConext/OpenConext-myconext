package myconext.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AccountLinkerController {

    private final String oidcBaseUrl;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpHeaders headers = new HttpHeaders();

    public AccountLinkerController(
            @Value("${oidc.client-id}") String clientId,
            @Value("${oidc.secret}") String clientSecret,
            @Value("${oidc.redirect-url}") String redirectUri,
            @Value("${oidc.base-url}") String oidcBaseUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.oidcBaseUrl = oidcBaseUrl;
        this.headers.set("Accept", "application/json");
    }

    @GetMapping("/idp/oidc/account")
    public ResponseEntity linkAccountRedirect() {
        Map<String, String> params = new HashMap<>();

        params.put("client_id", clientId);
        params.put("response_type", "code");
        params.put("scope", "openid");
        params.put("redirect_uri", redirectUri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(oidcBaseUrl + "/oidc/authorize");
        params.forEach(builder::queryParam);
        URI location = builder.build().toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @GetMapping("idp/oidc/redirect")
    public ResponseEntity redirect(@RequestParam("code") String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", this.redirectUri);
        map.add("scope", "openid");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        Map<String, Object> body = restTemplate.exchange(oidcBaseUrl + "/oidc/token", HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {
        }).getBody();

        map = new LinkedMultiValueMap<>();
        map.add("access_token", (String) body.get("access_token"));

        request = new HttpEntity<>(map, headers);

        body = restTemplate.exchange(oidcBaseUrl + "/oidc/userinfo", HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {
        }).getBody();

        //where do we go from here
        return null;
    }

}
