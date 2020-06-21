package myconext.oidcng;

import myconext.model.TokenRepresentation;
import myconext.model.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static myconext.log.MDCContext.mdcContext;

public class OpenIDConnectRemote implements OpenIDConnect {

    private static final Log LOG = LogFactory.getLog(OpenIDConnectRemote.class);

    private final RestTemplate restTemplate;
    private final HttpHeaders headers;
    private final URI oidcngUri;

    public OpenIDConnectRemote(URI oidcngUri,
                               String user,
                               String password) {
        this.restTemplate = new RestTemplate();
        this.oidcngUri = oidcngUri;

        this.headers = new HttpHeaders();
        this.headers.setContentType(MediaType.APPLICATION_JSON);
        this.headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        this.headers.setBasicAuth(user, password);
    }

    @Override
    public List<Map<String, Object>> tokens(User user) {
        String unspecifiedID = String.format("urn:collab:person:%s:%s", user.getSchacHomeOrganization(), user.getUid());

        mdcContext(Optional.of(user), "action", "oidcng-tokens", "unspecifiedID", unspecifiedID);
        LOG.info("Start fetching tokens from oidc-ng");

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        String uriString = UriComponentsBuilder.fromUri(oidcngUri)
                .queryParam("unspecifiedID", unspecifiedID)
                .toUriString();
        ResponseEntity<List<Map<String, Object>>> responseEntity =
                restTemplate.exchange(uriString, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });
        List<Map<String, Object>> body = responseEntity.getBody();
        LOG.info(String.format("Tokens result from oidc-ng %s", body));
        return body;
    }

    @Override
    public HttpStatus deleteTokens(List<TokenRepresentation> tokenIdentifiers) {
        mdcContext("action", "delete-oidc-tokens", "token-identifiers", tokenIdentifiers.toString());
        LOG.info("Deleting oidcng tokens");

        HttpEntity<List<TokenRepresentation>> requestEntity = new HttpEntity<>(tokenIdentifiers, headers);
        String uriString = UriComponentsBuilder.fromUri(oidcngUri).toUriString();
        ResponseEntity<Void> responseEntity = restTemplate.exchange(uriString, HttpMethod.PUT, requestEntity, Void.class);
        return responseEntity.getStatusCode();

    }

}
