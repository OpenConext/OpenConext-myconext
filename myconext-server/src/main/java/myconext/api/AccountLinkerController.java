package myconext.api;


import myconext.cron.DisposableEmailProviders;
import myconext.exceptions.ForbiddenException;
import myconext.exceptions.UserNotFoundException;
import myconext.mail.MailBox;
import myconext.manage.ServiceProviderResolver;
import myconext.model.*;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.RequestInstitutionEduIDRepository;
import myconext.repository.UserRepository;
import myconext.security.ACR;
import myconext.security.EmailGuessingPrevention;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static myconext.crypto.HashGenerator.hash;
import static myconext.log.MDCContext.logWithContext;
import static myconext.security.CookieResolver.cookieByName;
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
    private final String spCreateFromInstitutionRedirectUri;
    private final RestTemplate restTemplate = new RestTemplate();
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final RequestInstitutionEduIDRepository requestInstitutionEduIDRepository;
    private final UserRepository userRepository;
    private final MailBox mailBox;
    private final String magicLinkUrl;
    private final String idpErrorRedirectUrl;
    private final String spRedirectUrl;
    private final long removalNonValidatedDurationDays;
    private final long removalValidatedDurationDays;
    private final String idpExternalValidationEntityId;
    private final String myConextSpEntityId;
    private final boolean useExternalValidationFeature;
    private final ServiceProviderResolver serviceProviderResolver;
    private final String mijnEduIDEntityId;
    private final String schacHomeOrganization;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final EmailGuessingPrevention emailGuessingPreventor;
    private final DisposableEmailProviders disposableEmailProviders;

    public AccountLinkerController(
            AuthenticationRequestRepository authenticationRequestRepository,
            UserRepository userRepository,
            RequestInstitutionEduIDRepository requestInstitutionEduIDRepository,
            MailBox mailBox,
            ServiceProviderResolver serviceProviderResolver,
            DisposableEmailProviders disposableEmailProviders,
            @Value("${mijn_eduid_entity_id}") String mijnEduIDEntityId,
            @Value("${schac_home_organization}") String schacHomeOrganization,
            @Value("${email.magic-link-url}") String magicLinkUrl,
            @Value("${idp_redirect_url}") String idpErrorRedirectUrl,
            @Value("${sp_redirect_url}") String spRedirectUrl,
            @Value("${oidc.client-id}") String clientId,
            @Value("${oidc.secret}") String clientSecret,
            @Value("${oidc.idp-flow-redirect-url}") String idpFlowRedirectUri,
            @Value("${oidc.sp-flow-redirect-url}") String spFlowRedirectUri,
            @Value("${oidc.sp-create-from-institution-redirect-url}") String spCreateFromInstitutionRedirectUri,
            @Value("${oidc.base-url}") String oidcBaseUrl,
            @Value("${linked_accounts.removal-duration-days-non-validated}") long removalNonValidatedDurationDays,
            @Value("${linked_accounts.removal-duration-days-validated}") long removalValidatedDurationDays,
            @Value("${account_linking.idp_external_validation_entity_id}") String idpExternalValidationEntityId,
            @Value("${account_linking.myconext_sp_entity_id}") String myConextSpEntityId,
            @Value("${feature.use_external_validation}") boolean useExternalValidationFeature,
            @Value("${email_guessing_sleep_millis}") int emailGuessingSleepMillis) {
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.requestInstitutionEduIDRepository = requestInstitutionEduIDRepository;
        this.mailBox = mailBox;
        this.serviceProviderResolver = serviceProviderResolver;
        this.disposableEmailProviders = disposableEmailProviders;
        this.schacHomeOrganization = schacHomeOrganization;
        this.mijnEduIDEntityId = mijnEduIDEntityId;
        this.magicLinkUrl = magicLinkUrl;
        this.idpErrorRedirectUrl = idpErrorRedirectUrl;
        this.spRedirectUrl = spRedirectUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.idpFlowRedirectUri = idpFlowRedirectUri;
        this.spFlowRedirectUri = spFlowRedirectUri;
        this.spCreateFromInstitutionRedirectUri = spCreateFromInstitutionRedirectUri;
        this.oidcBaseUrl = oidcBaseUrl;
        this.removalNonValidatedDurationDays = removalNonValidatedDurationDays;
        this.removalValidatedDurationDays = removalValidatedDurationDays;
        this.idpExternalValidationEntityId = idpExternalValidationEntityId;
        this.myConextSpEntityId = myConextSpEntityId;
        this.useExternalValidationFeature = useExternalValidationFeature;
        this.emailGuessingPreventor = new EmailGuessingPrevention(emailGuessingSleepMillis);
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

    //Step 1 in the create-from-institution flow - will be called by the accessible CreateFromInstitution.svelte pag
    @GetMapping("/sp/oidc/create-from-institution")
    public ResponseEntity<Map<String, String>> createFromInstitution(HttpServletRequest request) throws UnsupportedEncodingException {
        LOG.debug("Start create from institution");
        String state = request.getSession(true).getId();
        UriComponents uriComponents = doStartLinkAccountFlow(state, spCreateFromInstitutionRedirectUri, false, false, myConextSpEntityId);
        return ResponseEntity.ok(Collections.singletonMap("url", uriComponents.toUriString()));
    }

    //Step 2 in the create-from-institution flow - will be called by the OpenIDConnect in the authorization flow
    @GetMapping("/sp/oidc/create-from-institution-redirect")
    public ResponseEntity spCreateFromInstitutionRedirect(HttpServletRequest request, @RequestParam("code") String code, @RequestParam("state") String state) throws UnsupportedEncodingException {
        String storedState = request.getSession(true).getId();

        if (MessageDigest.isEqual(
                storedState.getBytes(StandardCharsets.UTF_8),
                URLDecoder.decode(state, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8))) {
            throw new ForbiddenException("Non matching user");
        }
        Map<String, Object> userInfo = requestUserInfo(code, this.spCreateFromInstitutionRedirectUri);
        String eppn = (String) userInfo.get("eduperson_principal_name");
        //Check if the eppn is taken, before proceeding
        String eppnAlreadyLinkedRequiredUri = this.spRedirectUrl + "/eppn-already-linked?fromInstitution=true";
        Optional<ResponseEntity<Object>> eppnAlreadyLinkedOptional = checkEppnAlreadyLinked(eppnAlreadyLinkedRequiredUri, eppn);
        if (eppnAlreadyLinkedOptional.isPresent()) {
            return eppnAlreadyLinkedOptional.get();
        }
        //We want this also to work when mail is opened in different browser
        RequestInstitutionEduID requestInstitutionEduID = new RequestInstitutionEduID(hash(), userInfo);
        requestInstitutionEduIDRepository.save(requestInstitutionEduID);
        //Now the user needs to enter email, firstName and lastMail to finish up the registration
        String returnUri = this.spRedirectUrl + "/link-from-institution/" + requestInstitutionEduID.getHash();
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(returnUri)).build();
    }

    @PostMapping("/sp/create-from-institution/email")
    public void linkFromInstitution(@Valid @RequestBody CreateInstitutionEduID createInstitutionEduID) {
        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByHash(createInstitutionEduID.getHash())
                .orElseThrow(() -> new ForbiddenException("Wrong hash"));
        String email = createInstitutionEduID.getEmail();
        this.disposableEmailProviders.verifyDisposableEmailProviders(email);
        this.emailGuessingPreventor.potentialUserEmailGuess();

        userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(String.format("User with email %s not found", email)));
        requestInstitutionEduID.setCreateInstitutionEduID(createInstitutionEduID);
        requestInstitutionEduIDRepository.save(requestInstitutionEduID);

        String uri = spRedirectUrl + "/sp/create-from-institution/finish";
        mailBox.sendAccountVerificationCreateFromInstitution(new User(createInstitutionEduID), requestInstitutionEduID.getHash(), uri);
    }

    @GetMapping("/sp/create-from-institution/finish")
    public ResponseEntity createFromInstitutionFinish(HttpServletRequest request, @RequestParam("hash") String hash) throws UnsupportedEncodingException {
        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByHash(hash)
                .orElseThrow(() -> new ForbiddenException("Wrong hash"));
        CreateInstitutionEduID createInstitutionEduID = requestInstitutionEduID.getCreateInstitutionEduID();
        if (createInstitutionEduID == null) {
            throw new ForbiddenException("Wrong hash");
        }
        //Now we can create the User and populate the SecurityContext
        String preferredLanguage = cookieByName(request, "lang").map(Cookie::getValue).orElse("en");
        User user = new User(
                UUID.randomUUID().toString(),
                createInstitutionEduID.getEmail(),
                createInstitutionEduID.getGivenName(),
                createInstitutionEduID.getFamilyName(),
                schacHomeOrganization,
                preferredLanguage,
                mijnEduIDEntityId,
                serviceProviderResolver);
        ResponseEntity<Object> responseEntity = saveOrUpdateLinkedAccountToUser(
                user,
                this.spRedirectUrl + "/personal",
                false,
                false,
                null,
                null,
                null,
                requestInstitutionEduID.getUserInfo());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return responseEntity;
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
    private ResponseEntity doRedirect(@RequestParam("code") String code,
                                      User user,
                                      String oidcRedirectUri,
                                      String clientRedirectUri,
                                      boolean validateNames,
                                      boolean studentAffiliationRequired,
                                      String idpStudentAffiliationRequiredUri,
                                      String idpValidNamesRequiredUri,
                                      String eppnAlreadyLinkedRequiredUri) throws UnsupportedEncodingException {
        Map<String, Object> body = requestUserInfo(code, oidcRedirectUri);

        return saveOrUpdateLinkedAccountToUser(user, clientRedirectUri, validateNames, studentAffiliationRequired, idpStudentAffiliationRequiredUri, idpValidNamesRequiredUri, eppnAlreadyLinkedRequiredUri, body);
    }

    private ResponseEntity<Object> saveOrUpdateLinkedAccountToUser(User user,
                                                                   String clientRedirectUri,
                                                                   boolean validateNames,
                                                                   boolean studentAffiliationRequired,
                                                                   String idpStudentAffiliationRequiredUri,
                                                                   String idpValidNamesRequiredUri,
                                                                   String eppnAlreadyLinkedRequiredUri,
                                                                   Map<String, Object> body) throws UnsupportedEncodingException {
        String eppn = (String) body.get("eduperson_principal_name");
        String subjectId = (String) body.get("subject_id");
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
                optionalLinkedAccount.get().updateExpiresIn(institutionIdentifier, eppn, subjectId, givenName, familyName, affiliations, expiresAt);
            } else {
                Optional<ResponseEntity<Object>> eppnAlreadyLinkedOptional = checkEppnAlreadyLinked(eppnAlreadyLinkedRequiredUri, eppn);
                if (eppnAlreadyLinkedOptional.isPresent()) {
                    return eppnAlreadyLinkedOptional.get();
                }
                linkedAccounts.add(
                        new LinkedAccount(institutionIdentifier, schacHomeOrganization, eppn, subjectId, givenName, familyName, affiliations,
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

    private Optional<ResponseEntity<Object>> checkEppnAlreadyLinked(String eppnAlreadyLinkedRequiredUri, String eppn) throws UnsupportedEncodingException {
        //Ensure that an institution account is only be linked to 1 eduID, but only when an eppn is provided for the linked account
        if (StringUtils.hasText(eppn)) {
            List<User> optionalUsers = userRepository.findByLinkedAccounts_EduPersonPrincipalName(eppn);
            if (optionalUsers.size() > 0) {
                String charSet = Charset.defaultCharset().name();
                eppnAlreadyLinkedRequiredUri += eppnAlreadyLinkedRequiredUri.contains("?") ? "&" : "?";
                eppnAlreadyLinkedRequiredUri += "email=" + URLEncoder.encode(optionalUsers.get(0).getEmail(), charSet);
                return Optional.of(ResponseEntity.status(HttpStatus.FOUND).location(URI.create(eppnAlreadyLinkedRequiredUri)).build());
            }
        }
        return Optional.empty();
    }

    private Map<String, Object> requestUserInfo(String code, String oidcRedirectUri) {
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

        ParameterizedTypeReference<Map<String, Object>> parameterizedTypeReference = new ParameterizedTypeReference<>() {
        };
        Map<String, Object> body = restTemplate.exchange(oidcBaseUrl + "/oidc/token", HttpMethod.POST, request, parameterizedTypeReference).getBody();

        map = new LinkedMultiValueMap<>();
        map.add("access_token", (String) body.get("access_token"));

        request = new HttpEntity<>(map, headers);

        body = restTemplate.exchange(oidcBaseUrl + "/oidc/userinfo", HttpMethod.POST, request, parameterizedTypeReference).getBody();
        return body;
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
