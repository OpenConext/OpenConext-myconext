package myconext.oidcng;

import myconext.model.TokenRepresentation;
import myconext.model.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static myconext.log.MDCContext.logWithContext;


public class OpenIDConnectRemote implements OpenIDConnect {

    private static final Log LOG = LogFactory.getLog(OpenIDConnectRemote.class);

    private final RestTemplate restTemplate;
    private final HttpHeaders headers;
    private final URI oidcngUri;
    private final boolean featureOidcTokenAPI;

    public OpenIDConnectRemote(URI oidcngUri,
                               String user,
                               String password,
                               boolean featureOidcTokenAPI) {
        this.restTemplate = new RestTemplate();
        this.oidcngUri = oidcngUri;
        this.featureOidcTokenAPI = featureOidcTokenAPI;

        this.headers = new HttpHeaders();
        this.headers.setContentType(MediaType.APPLICATION_JSON);
        this.headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        this.headers.setBasicAuth(user, password);
    }

    @Override
    public List<Map<String, Object>> tokens(User user) {
        if (!featureOidcTokenAPI) {
            return Collections.emptyList();
        }
        String unspecifiedID = String.format("urn:collab:person:%s:%s", user.getSchacHomeOrganization(), user.getUid());

        LOG.info(String.format("Fetching tokens from oidc-ng for %s", user.getEmail()));

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        String uriString = UriComponentsBuilder.fromUri(oidcngUri)
                .queryParam("unspecifiedID", unspecifiedID)
                .toUriString();
        ResponseEntity<List<Map<String, Object>>> responseEntity =
                restTemplate.exchange(uriString, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });
        List<Map<String, Object>> body = responseEntity.getBody();
        LOG.debug(String.format("Tokens result from oidc-ng %s", body));
        return body;
    }

    @Override
    public HttpStatus deleteTokens(List<TokenRepresentation> tokenIdentifiers, User user) {
        if (!featureOidcTokenAPI) {
            return HttpStatus.OK;
        }
        logWithContext(user, "delete", "tokens", LOG, "Deleting oidcng tokens");

        HttpEntity<List<TokenRepresentation>> requestEntity = new HttpEntity<>(tokenIdentifiers, headers);
        String uriString = UriComponentsBuilder.fromUri(oidcngUri).toUriString();
        ResponseEntity<Void> responseEntity = restTemplate.exchange(uriString, HttpMethod.PUT, requestEntity, Void.class);
        return responseEntity.getStatusCode();

    }

}
