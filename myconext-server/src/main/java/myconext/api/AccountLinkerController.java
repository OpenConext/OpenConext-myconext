package myconext.api;

import myconext.exceptions.UserNotFoundException;
import myconext.model.LinkedAccount;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static myconext.security.GuestIdpAuthenticationRequestFilter.EDUPERSON_SCOPED_AFFILIATION_SAML;
import static myconext.security.GuestIdpAuthenticationRequestFilter.EDUPERSON_SCOPED_AFFILIATION_VERIFIED_BY_INSTITUTION;

@RestController
@RequestMapping("/myconext/api")
public class AccountLinkerController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountLinkerController.class);

    private final String oidcBaseUrl;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final RestTemplate restTemplate = new RestTemplate();
    private final HttpHeaders headers = new HttpHeaders();
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;
    private final String magicLinkUrl;
    private final String idpRedirectUrl;
    private final long expiryDurationDays;

    public AccountLinkerController(
            AuthenticationRequestRepository authenticationRequestRepository,
            UserRepository userRepository,
            @Value("${email.magic-link-url}") String magicLinkUrl,
            @Value("${idp_redirect_url}") String idpRedirectUrl,
            @Value("${oidc.client-id}") String clientId,
            @Value("${oidc.secret}") String clientSecret,
            @Value("${oidc.redirect-url}") String redirectUri,
            @Value("${oidc.base-url}") String oidcBaseUrl,
            @Value("${oidc.expiry-duration-days}") long expiryDurationDays) {
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.magicLinkUrl = magicLinkUrl;
        this.idpRedirectUrl = idpRedirectUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.oidcBaseUrl = oidcBaseUrl;
        this.expiryDurationDays = expiryDurationDays;
        this.headers.set("Accept", "application/json");
    }

    @GetMapping("/idp/oidc/account/{id}")
    public ResponseEntity linkAccountRedirect(@PathVariable("id") String id) {
        Map<String, String> params = new HashMap<>();

        params.put("client_id", clientId);
        params.put("response_type", "code");
        params.put("scope", "openid");
        params.put("redirect_uri", redirectUri);
        params.put("state", id);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(oidcBaseUrl + "/oidc/authorize");
        params.forEach(builder::queryParam);
        URI location = builder.build().toUri();
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @GetMapping("/idp/oidc/redirect")
    public ResponseEntity redirect(@RequestParam("code") String code, @RequestParam("state") String state) {
        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(state);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(this.idpRedirectUrl + "/expired")).build();
        }

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

        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();
        User user = userRepository.findById(samlAuthenticationRequest.getUserId())
                .orElseThrow(UserNotFoundException::new);
        user.getAttributes()
                .computeIfAbsent(EDUPERSON_SCOPED_AFFILIATION_SAML, key -> new ArrayList<>())
                .add(EDUPERSON_SCOPED_AFFILIATION_VERIFIED_BY_INSTITUTION);

        String eppn = (String) body.get("eduperson_principal_name");
        String surfCrmId = (String) body.get("surf-crm-id");
        String schacHomeOrganization = (String) body.get("schac_home_organization");

        Date expiresAt = Date.from(new Date().toInstant().plus(expiryDurationDays, ChronoUnit.DAYS));
        List<LinkedAccount> linkedAccounts = user.getLinkedAccounts();
        String institutionIdentifier = StringUtils.hasText(surfCrmId) ? surfCrmId : schacHomeOrganization;
        boolean newLinkedAccountAdded = linkedAccounts.stream()
                .filter(linkedAccount -> linkedAccount.getInstitutionIdentifier().equals(surfCrmId) ||
                        linkedAccount.getInstitutionIdentifier().equals(schacHomeOrganization))
                .findFirst()
                .map(linkedAccount -> linkedAccount.updateExpiresIn(institutionIdentifier, expiresAt))
                .orElse(linkedAccounts.add(
                        new LinkedAccount(institutionIdentifier, schacHomeOrganization, eppn, new Date(), expiresAt)));
        String action = newLinkedAccountAdded ? "created" : "updated";
        String eppnValue = StringUtils.hasText(eppn) ? String.format("eppn %s", eppn) : "NO eppn";

        LOG.info("An account link has been {} for User {} with {} to institution {}",
                action, user.getEmail(), eppnValue, institutionIdentifier);

        userRepository.save(user);

        String location = this.magicLinkUrl + "?h=" + samlAuthenticationRequest.getHash();
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(location)).build();
    }

}
