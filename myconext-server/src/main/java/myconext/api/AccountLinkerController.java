package myconext.api;


import myconext.exceptions.ForbiddenException;
import myconext.exceptions.UserNotFoundException;
import myconext.model.LinkedAccount;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.StepUpStatus;
import myconext.model.User;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.UserRepository;
import myconext.security.ACR;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static myconext.log.MDCContext.logWithContext;
import static myconext.security.GuestIdpAuthenticationRequestFilter.hasRequiredStudentAffiliation;
import static myconext.security.GuestIdpAuthenticationRequestFilter.hasValidatedName;

@RestController
@RequestMapping("/myconext/api")
@SuppressWarnings("unchecked")

public class AccountLinkerController {

    private static final Log LOG = LogFactory.getLog(AccountLinkerController.class);

    private final String oidcBaseUrl;
    private final String clientId;
    private final String clientSecret;
    private final String idpFlowRedirectUri;
    private final String spFlowRedirectUri;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;
    private final String magicLinkUrl;
    private final String idpErrorRedirectUrl;
    private final String spRedirectUrl;
    private final long removalNonValidatedDurationDays;
    private final long removalValidatedDurationDays;
    private final String idpExternalValidationEntityId;
    private final String myConextSpEntityId;
    private final boolean useExternalValidationFeature;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AccountLinkerController(
            AuthenticationRequestRepository authenticationRequestRepository,
            UserRepository userRepository,
            @Value("${email.magic-link-url}") String magicLinkUrl,
            @Value("${idp_redirect_url}") String idpErrorRedirectUrl,
            @Value("${sp_redirect_url}") String spRedirectUrl,
            @Value("${oidc.client-id}") String clientId,
            @Value("${oidc.secret}") String clientSecret,
            @Value("${oidc.idp-flow-redirect-url}") String idpFlowRedirectUri,
            @Value("${oidc.sp-flow-redirect-url}") String spFlowRedirectUri,
            @Value("${oidc.base-url}") String oidcBaseUrl,
            @Value("${linked_accounts.removal-duration-days-non-validated}") long removalNonValidatedDurationDays,
            @Value("${linked_accounts.removal-duration-days-validated}") long removalValidatedDurationDays,
            @Value("${account_linking.idp_external_validation_entity_id}") String idpExternalValidationEntityId,
            @Value("${account_linking.myconext_sp_entity_id}") String myConextSpEntityId,
            @Value("${feature.use_external_validation}") boolean useExternalValidationFeature) {
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.magicLinkUrl = magicLinkUrl;
        this.idpErrorRedirectUrl = idpErrorRedirectUrl;
        this.spRedirectUrl = spRedirectUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.idpFlowRedirectUri = idpFlowRedirectUri;
        this.spFlowRedirectUri = spFlowRedirectUri;
        this.oidcBaseUrl = oidcBaseUrl;
        this.removalNonValidatedDurationDays = removalNonValidatedDurationDays;
        this.removalValidatedDurationDays = removalValidatedDurationDays;
        this.idpExternalValidationEntityId = idpExternalValidationEntityId;
        this.myConextSpEntityId = myConextSpEntityId;
        this.useExternalValidationFeature = useExternalValidationFeature;
    }

    @GetMapping("/idp/oidc/account/{id}")
    public ResponseEntity startIdPLinkAccountFlow(@PathVariable("id") String id,
                                                  @RequestParam(value = "forceAuth", required = false, defaultValue = "false") boolean forceAuth,
                                                  @RequestParam(value = "useExternalValidation", required = false, defaultValue = "false") boolean useExternalValidation) throws UnsupportedEncodingException {
        LOG.debug("Start IdP link account flow");

        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(id);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(this.idpErrorRedirectUrl + "/expired")).build();
        }
        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();

        //when there are retries we remember the choice of the user (for now)
        if (useExternalValidation && useExternalValidationFeature && !samlAuthenticationRequest.isUseExternalValidation()) {
            samlAuthenticationRequest.setUseExternalValidation(true);
            authenticationRequestRepository.save(samlAuthenticationRequest);
        }
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        String state = String.format("id=%s&user_uid=%s", id, passwordEncoder.encode(user.getUid()));
        UriComponents uriComponents = doStartLinkAccountFlow(state, idpFlowRedirectUri, forceAuth,
                samlAuthenticationRequest.isUseExternalValidation(), samlAuthenticationRequest.getRequesterEntityId());
        return ResponseEntity.status(HttpStatus.FOUND).location(uriComponents.toUri()).build();
    }

    @GetMapping("/sp/oidc/link")
    public ResponseEntity startSPLinkAccountFlow(Authentication authentication) throws UnsupportedEncodingException {
        LOG.debug("Start link account flow");
        User principal = (User) authentication.getPrincipal();
        String state = passwordEncoder.encode(principal.getUid());

        UriComponents uriComponents = doStartLinkAccountFlow(state, spFlowRedirectUri, true, false, myConextSpEntityId);
        return ResponseEntity.ok(Collections.singletonMap("url", uriComponents.toUriString()));
    }

    private UriComponents doStartLinkAccountFlow(String state, String redirectUri, boolean forceAuth, boolean useExternalValidation, String requesterEntityId) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();

        params.put("client_id", clientId);
        params.put("response_type", "code");
        params.put("scope", "openid");
        params.put("redirect_uri", redirectUri);
        params.put("state", URLEncoder.encode(state, "UTF-8"));
        if (forceAuth) {
            params.put("prompt", "login");
        }
        if (useExternalValidation) {
            params.put("login_hint", this.idpExternalValidationEntityId);
            params.put("acr_values", requesterEntityId);
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(oidcBaseUrl + "/oidc/authorize");
        params.forEach(builder::queryParam);
        return builder.build();
    }

    @GetMapping("/sp/oidc/redirect")
    public ResponseEntity spFlowRedirect(Authentication authentication, @RequestParam("code") String code, @RequestParam("state") String state) throws UnsupportedEncodingException {
        User principal = (User) authentication.getPrincipal();
        String uid = principal.getUid();
        Optional<User> userOptional = userRepository.findUserByUid(uid);
        User user = userOptional.orElseThrow(() -> new UserNotFoundException(uid));

        if (!passwordEncoder.matches(user.getUid(), URLDecoder.decode(state, "UTF-8"))) {
            throw new ForbiddenException("Non matching user");
        }

        LOG.debug("In SP redirect link account");

        return doRedirect(code, user, this.spFlowRedirectUri, this.spRedirectUrl + "/personal",
                false, false, null, null,
                this.spRedirectUrl + "/eppn-already-linked");
    }

    @GetMapping("/idp/oidc/redirect")
    public ResponseEntity idpFlowRedirect(HttpServletRequest request, @RequestParam("code") String code, @RequestParam("state") String state) throws UnsupportedEncodingException {
        String decodedState = URLDecoder.decode(state, "UTF-8");
        MultiValueMap<String, String> params = UriComponentsBuilder.fromHttpUrl("http://localhost?" + decodedState).build().getQueryParams();
        String id = params.getFirst("id");
        String encodedUserUid = params.getFirst("user_uid");

        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(id);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(this.idpErrorRedirectUrl + "/expired")).build();
        }
        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(user.getUid(), encodedUserUid)) {
            throw new ForbiddenException("Non matching user");
        }

        boolean validateNames = samlAuthenticationRequest.getAuthenticationContextClassReferences().contains(ACR.VALIDATE_NAMES);
        boolean studentAffiliationRequired = samlAuthenticationRequest.getAuthenticationContextClassReferences().contains(ACR.AFFILIATION_STUDENT);

        LOG.debug("In IdP redirect link account");

        String location = this.magicLinkUrl + "?h=" + samlAuthenticationRequest.getHash();

        String charSet = Charset.defaultCharset().name();

        String idpStudentAffiliationRequiredUri = this.idpErrorRedirectUrl + "/affiliation-missing/" +
                samlAuthenticationRequest.getId() +
                "?h=" + samlAuthenticationRequest.getHash() +
                "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet);

        String idpValidNamesRequiredUri = this.idpErrorRedirectUrl + "/valid-name-missing/" +
                samlAuthenticationRequest.getId() +
                "?h=" + samlAuthenticationRequest.getHash() +
                "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet);

        String eppnAlreadyLinkedRequiredUri = this.idpErrorRedirectUrl + "/eppn-already-linked/" +
                samlAuthenticationRequest.getId() +
                "?h=" + samlAuthenticationRequest.getHash() +
                "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet);

        ResponseEntity redirect = doRedirect(code, user, this.idpFlowRedirectUri, location, validateNames, studentAffiliationRequired,
                idpStudentAffiliationRequiredUri, idpValidNamesRequiredUri, eppnAlreadyLinkedRequiredUri);

        StepUpStatus stepUpStatus = redirect.getHeaders().getLocation()
                .toString().contains("affiliation-missing") ? StepUpStatus.MISSING_AFFILIATION : StepUpStatus.IN_STEP_UP;
        samlAuthenticationRequest.setSteppedUp(stepUpStatus);
        authenticationRequestRepository.save(samlAuthenticationRequest);

        return redirect;
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity doRedirect(@RequestParam("code") String code, User user, String oidcRedirectUri,
                                      String clientRedirectUri, boolean validateNames, boolean studentAffiliationRequired,
                                      String idpStudentAffiliationRequiredUri, String idpValidNamesRequiredUri,
                                      String eppnAlreadyLinkedRequiredUri) throws UnsupportedEncodingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", oidcRedirectUri);
        map.add("scope", "openid");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ParameterizedTypeReference<Map<String, Object>> parameterizedTypeReference = new ParameterizedTypeReference<Map<String, Object>>() {
        };
        Map<String, Object> body = restTemplate.exchange(oidcBaseUrl + "/oidc/token", HttpMethod.POST, request, parameterizedTypeReference).getBody();

        map = new LinkedMultiValueMap<>();
        map.add("access_token", (String) body.get("access_token"));

        request = new HttpEntity<>(map, headers);

        body = restTemplate.exchange(oidcBaseUrl + "/oidc/userinfo", HttpMethod.POST, request, parameterizedTypeReference).getBody();

        String eppn = (String) body.get("eduperson_principal_name");
        String surfCrmId = (String) body.get("surf-crm-id");
        String schacHomeOrganization = (String) body.get("schac_home_organization");

        String givenName = (String) body.get("given_name");
        String familyName = (String) body.get("family_name");

        String institutionIdentifier = StringUtils.hasText(surfCrmId) ? surfCrmId : schacHomeOrganization;

        List<String> affiliations = parseAffiliations(body, schacHomeOrganization);

        if (StringUtils.hasText(schacHomeOrganization)) {
            Date expiresAt = Date.from(new Date().toInstant()
                    .plus(validateNames ? this.removalValidatedDurationDays : this.removalNonValidatedDurationDays, ChronoUnit.DAYS));
            List<LinkedAccount> linkedAccounts = user.getLinkedAccounts();
            Optional<LinkedAccount> optionalLinkedAccount = linkedAccounts.stream()
                    .filter(linkedAccount -> linkedAccount.getSchacHomeOrganization().equals(schacHomeOrganization))
                    .findFirst();
            if (optionalLinkedAccount.isPresent()) {
                optionalLinkedAccount.get().updateExpiresIn(institutionIdentifier, eppn, givenName, familyName, affiliations, expiresAt);
            } else {
                //Ensure that an institution account is only be linked to 1 eduID, but only when an eppn is provided for the linked account
                if (StringUtils.hasText(eppn)) {
                    List<User> optionalUsers = userRepository.findByLinkedAccounts_EduPersonPrincipalName(eppn);
                    if (optionalUsers.size() > 0) {
                        String charSet = Charset.defaultCharset().name();
                        eppnAlreadyLinkedRequiredUri += eppnAlreadyLinkedRequiredUri.contains("?") ? "&" : "?";
                        eppnAlreadyLinkedRequiredUri += "email=" + URLEncoder.encode(optionalUsers.get(0).getEmail(), charSet);
                        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(eppnAlreadyLinkedRequiredUri)).build();
                    }
                }
                linkedAccounts.add(
                        new LinkedAccount(institutionIdentifier, schacHomeOrganization, eppn, givenName, familyName, affiliations,
                                new Date(), expiresAt));
            }
            String action = optionalLinkedAccount.isPresent() ? "updated" : "add";
            String eppnValue = StringUtils.hasText(eppn) ? String.format("eppn %s", eppn) : "NO eppn";

            logWithContext(user, action, "linked_accounts", LOG, String.format("Account link with EPPN %s for institution %s with the affiliations %s",
                    eppnValue, institutionIdentifier, affiliations));

            userRepository.save(user);
        } else {
            LOG.error("Account linking requested, but no schacHomeOrganization provided by the IdP");
        }
        boolean hasStudentAffiliation = user.getLinkedAccounts().stream()
                .anyMatch(linkedAccount -> hasRequiredStudentAffiliation(linkedAccount.getEduPersonAffiliations()));
        if (studentAffiliationRequired && !hasStudentAffiliation) {
            //Corner case where the user has been stepped Up, but there is no student affiliation
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(idpStudentAffiliationRequiredUri)).build();
        }

        boolean hasValidNames = hasValidatedName(user);
        if (validateNames && !hasValidNames) {
            //Corner case where the user has been stepped Up, but there is no valid name
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(idpValidNamesRequiredUri)).build();
        }

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(clientRedirectUri)).build();
    }

    protected static List<String> parseAffiliations(Map<String, Object> idpAttributes, String schacHomeOrganization) {
        if (!StringUtils.hasText(schacHomeOrganization)) {
            return new ArrayList<>();
        }
        List<String> eduPersonAffiliations = ((List<String>) idpAttributes.getOrDefault("eduperson_affiliation", new ArrayList<>()))
                .stream().map(affiliation -> String.format("%s@%s", affiliation, schacHomeOrganization)).collect(Collectors.toList());
        List<String> eduPersonScopedAffiliations = (List<String>) idpAttributes.getOrDefault("eduperson_scoped_affiliation", eduPersonAffiliations);
        Set<String> uniqueAffiliations = new HashSet<>(eduPersonAffiliations);
        uniqueAffiliations.addAll(eduPersonScopedAffiliations);
        return CollectionUtils.isEmpty(uniqueAffiliations) ? Arrays.asList(String.format("affiliate@%s", schacHomeOrganization)) : new ArrayList<>(uniqueAffiliations);
    }

}
