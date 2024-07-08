package myconext.api;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import myconext.cron.DisposableEmailProviders;
import myconext.exceptions.DuplicateUserEmailException;
import myconext.exceptions.ForbiddenException;
import myconext.exceptions.UserNotFoundException;
import myconext.mail.MailBox;
import myconext.manage.Manage;
import myconext.model.*;
import myconext.repository.AuthenticationRequestRepository;
import myconext.repository.MobileLinkAccountRequestRepository;
import myconext.repository.RequestInstitutionEduIDRepository;
import myconext.repository.UserRepository;
import myconext.security.ACR;
import myconext.security.EmailGuessingPrevention;
import myconext.security.UserAuthentication;
import myconext.verify.AttributeMapper;
import myconext.verify.VerifyState;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
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
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
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
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@RestController
@RequestMapping(value = {"/myconext/api", "/mobile/api"})
@SuppressWarnings("unchecked")
public class AccountLinkerController implements UserAuthentication {

    private static final Log LOG = LogFactory.getLog(AccountLinkerController.class);

    private final String oidcBaseUrl;
    private final String clientId;
    private final String clientSecret;
    private final String idpFlowRedirectUri;
    private final String spFlowRedirectUri;
    private final String mobileFlowRedirectUri;
    private final String spCreateFromInstitutionRedirectUri;
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final RequestInstitutionEduIDRepository requestInstitutionEduIDRepository;
    private final MobileLinkAccountRequestRepository mobileLinkAccountRequestRepository;
    private final UserRepository userRepository;
    private final MailBox mailBox;
    private final AttributeMapper attributeMapper;
    private final String magicLinkUrl;
    private final String idpBaseRedirectUrl;
    private final String spRedirectUrl;
    private final String basePath;
    private final long removalValidatedDurationDays;
    private final String myConextSpEntityId;
    private final Manage manage;
    private final String mijnEduIDEntityId;
    private final String schacHomeOrganization;
    private final boolean createEduIDInstitutionEnabled;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final RestTemplate restTemplate = new RestTemplate();
    private final EmailGuessingPrevention emailGuessingPreventor;
    private final DisposableEmailProviders disposableEmailProviders;
    private final String verifySecret;
    private final String verifyClientId;
    private final String verifyBaseUri;
    private final String spVerifyRedirectUri;
    private final String mobileVerifyRedirectUri;
    private final String idpVerifyRedirectUri;

    private final List<VerifyIssuer> issuers;
    //For now, hardcode the not known issuers from test
    private final List<String> unknownIssuers = List.of("CURRNL2A");

    public AccountLinkerController(
            AuthenticationRequestRepository authenticationRequestRepository,
            UserRepository userRepository,
            RequestInstitutionEduIDRepository requestInstitutionEduIDRepository,
            MobileLinkAccountRequestRepository mobileLinkAccountRequestRepository,
            MailBox mailBox,
            AttributeMapper attributeMapper,
            Manage manage,
            DisposableEmailProviders disposableEmailProviders,
            @Value("${mijn_eduid_entity_id}") String mijnEduIDEntityId,
            @Value("${schac_home_organization}") String schacHomeOrganization,
            @Value("${email.magic-link-url}") String magicLinkUrl,
            @Value("${idp_redirect_url}") String idpBaseRedirectUrl,
            @Value("${sp_redirect_url}") String spRedirectUrl,
            @Value("${oidc.client-id}") String clientId,
            @Value("${oidc.secret}") String clientSecret,
            @Value("${oidc.idp-flow-redirect-url}") String idpFlowRedirectUri,
            @Value("${oidc.sp-flow-redirect-url}") String spFlowRedirectUri,
            @Value("${oidc.mobile-flow-redirect-url}") String mobileFlowRedirectUri,
            @Value("${oidc.sp-create-from-institution-redirect-url}") String spCreateFromInstitutionRedirectUri,
            @Value("${oidc.base-url}") String oidcBaseUrl,
            @Value("${base_path}") String basePath,
            @Value("${linked_accounts.removal-duration-days-validated}") long removalValidatedDurationDays,
            @Value("${account_linking.myconext_sp_entity_id}") String myConextSpEntityId,
            @Value("${feature.create_eduid_institution_enabled}") boolean createEduIDInstitutionEnabled,
            @Value("${email_guessing_sleep_millis}") int emailGuessingSleepMillis,
            @Value("${verify.client_id}") String verifyClientId,
            @Value("${verify.secret}") String verifySecret,
            @Value("${verify.sp_verify_redirect_url}") String spVerifyRedirectUri,
            @Value("${verify.idp_verify_redirect_url}") String idpVerifyRedirectUri,
            @Value("${verify.mobile_verify_redirect_url}") String mobileVerifyRedirectUri,
            @Value("${verify.base_uri}") String verifyBaseUri,
            @Value("${verify.issuers_path}") Resource issuersResource,
            ObjectMapper objectMapper) throws IOException {
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.requestInstitutionEduIDRepository = requestInstitutionEduIDRepository;
        this.mobileLinkAccountRequestRepository = mobileLinkAccountRequestRepository;
        this.mailBox = mailBox;
        this.attributeMapper = attributeMapper;
        this.manage = manage;
        this.disposableEmailProviders = disposableEmailProviders;
        this.schacHomeOrganization = schacHomeOrganization;
        this.mijnEduIDEntityId = mijnEduIDEntityId;
        this.magicLinkUrl = magicLinkUrl;
        this.idpBaseRedirectUrl = idpBaseRedirectUrl;
        this.spRedirectUrl = spRedirectUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.idpFlowRedirectUri = idpFlowRedirectUri;
        this.spFlowRedirectUri = spFlowRedirectUri;
        this.mobileFlowRedirectUri = mobileFlowRedirectUri;
        this.spCreateFromInstitutionRedirectUri = spCreateFromInstitutionRedirectUri;
        this.basePath = basePath;
        this.oidcBaseUrl = oidcBaseUrl;
        this.removalValidatedDurationDays = removalValidatedDurationDays;
        this.myConextSpEntityId = myConextSpEntityId;
        this.createEduIDInstitutionEnabled = createEduIDInstitutionEnabled;
        this.emailGuessingPreventor = new EmailGuessingPrevention(emailGuessingSleepMillis);
        this.verifyClientId = verifyClientId;
        this.verifySecret = verifySecret;
        this.verifyBaseUri = verifyBaseUri;
        this.mobileVerifyRedirectUri = mobileVerifyRedirectUri;
        this.spVerifyRedirectUri = spVerifyRedirectUri;
        this.idpVerifyRedirectUri = idpVerifyRedirectUri;

        List<IdinIssuers> idinIssuers = objectMapper.readValue(issuersResource.getInputStream(), new TypeReference<>() {
        });
        //For now, we only support "Nederland"
        this.issuers = idinIssuers.get(0).getIssuers().stream().filter(issuer -> !unknownIssuers.contains(issuer.getId())).collect(Collectors.toList());
        LOG.debug(String.format("Initialized IDIN issuers %s from %s", this.issuers, issuersResource.getDescription()));
    }

    @GetMapping("/idp/oidc/account/{id}")
    @Hidden
    public ResponseEntity startIdPLinkAccountFlow(@PathVariable("id") String id,
                                                  @RequestParam(value = "forceAuth", required = false, defaultValue = "false") boolean forceAuth) throws UnsupportedEncodingException {

        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(id);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(this.idpBaseRedirectUrl + "/expired")).build();
        }
        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();

        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        LOG.info(String.format("Start IdP link account flow for user %s", user.getEmail()));

        String state = String.format("id=%s&user_uid=%s", id, passwordEncoder.encode(user.getUid()));
        UriComponents uriComponents = doStartLinkAccountFlow(state, idpFlowRedirectUri, forceAuth, samlAuthenticationRequest.getRequesterEntityId());
        return ResponseEntity.status(HttpStatus.FOUND).location(uriComponents.toUri()).build();
    }

    @GetMapping("/sp/create-from-institution")
    @Hidden
    public ResponseEntity<Map<String, String>> createFromInstitution(HttpServletRequest request,
                                                                     @RequestParam(value = "forceAuth", required = false, defaultValue = "false") boolean forceAuth) throws UnsupportedEncodingException {
        LOG.info("Start create from institution");
        String state = request.getSession(true).getId();
        UriComponents uriComponents = doStartLinkAccountFlow(state, spCreateFromInstitutionRedirectUri, forceAuth, myConextSpEntityId);
        return ResponseEntity.ok(Collections.singletonMap("url", uriComponents.toUriString()));
    }

    @GetMapping("/sp/create-from-institution/oidc-redirect")
    @Hidden
    public ResponseEntity spCreateFromInstitutionRedirect(HttpServletRequest request, @RequestParam("code") String code, @RequestParam("state") String state) throws UnsupportedEncodingException {
        LOG.info("In redirect for create-institution-flow");

        HttpSession session = request.getSession(false);
        if (session == null || !createEduIDInstitutionEnabled) {
            throw new ForbiddenException("No session enabled");
        }
        String storedState = session.getId();

        if (!MessageDigest.isEqual(
                storedState.getBytes(StandardCharsets.UTF_8),
                URLDecoder.decode(state, StandardCharsets.UTF_8).getBytes(StandardCharsets.UTF_8))) {
            throw new ForbiddenException("No session enabled");
        }
        Map<String, Object> userInfo = requestUserInfo(code, this.spCreateFromInstitutionRedirectUri);
        String eppn = (String) userInfo.get("eduperson_principal_name");
        //Check if the eppn is taken, before proceeding
        String eppnAlreadyLinkedRequiredUri = this.spRedirectUrl + "/create-from-institution/eppn-already-linked?fromInstitution=true";
        Optional<ResponseEntity<Object>> eppnAlreadyLinkedOptional = checkEppnAlreadyLinked(eppnAlreadyLinkedRequiredUri, eppn);
        if (eppnAlreadyLinkedOptional.isPresent()) {
            LOG.debug("EPPN already linked in create-institution-flow for " + eppn);
            return eppnAlreadyLinkedOptional.get();
        }
        //We want this also to work when mail is opened in different browser
        RequestInstitutionEduID requestInstitutionEduID = new RequestInstitutionEduID(hash(), userInfo);
        requestInstitutionEduIDRepository.save(requestInstitutionEduID);
        //Now the user needs to enter email and validate this email to finish up the registration
        String returnUri = this.spRedirectUrl + "/create-from-institution/link/" + requestInstitutionEduID.getHash();
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(returnUri)).build();
    }

    @GetMapping("/sp/create-from-institution/info")
    @Hidden
    public Map<String, Object> createFromInstitutionInfo(@RequestParam("hash") String hash) {
        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByHash(hash)
                .orElseThrow(() -> new ForbiddenException("Wrong hash"));

        LOG.info(String.format("Info details for create-institution-flow %s", requestInstitutionEduID));

        return requestInstitutionEduID.getUserInfo();
    }

    @PostMapping("/sp/create-from-institution/email")
    @Hidden
    public ResponseEntity<Map<String, String>> linkFromInstitution(@Valid @RequestBody CreateInstitutionEduID createInstitutionEduID) {

        LOG.info(String.format("Post details for account verification in create-institution-flow %s", createInstitutionEduID.getEmail()));

        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByHash(createInstitutionEduID.getHash())
                .orElseThrow(() -> new ForbiddenException("Wrong hash"));
        String email = createInstitutionEduID.getEmail();
        this.disposableEmailProviders.verifyDisposableEmailProviders(email);
        this.emailGuessingPreventor.potentialUserEmailGuess();

        Optional<User> userByEmail = userRepository.findUserByEmail(email);
        boolean newUser = createInstitutionEduID.isNewUser();
        if (newUser && userByEmail.isPresent()) {
            throw new DuplicateUserEmailException("There already exists a user with email " + email);
        }
        if (!newUser && userByEmail.isEmpty()) {
            throw new UserNotFoundException("User not found: " + email);
        }
        User user = userByEmail.orElse(new User(createInstitutionEduID, requestInstitutionEduID.getUserInfo()));
        requestInstitutionEduID.setUserId(user.getId());

        requestInstitutionEduID.setCreateInstitutionEduID(createInstitutionEduID);
        //We need a new Hash for email verification, one that is not exposed to the browser
        requestInstitutionEduID.setEmailHash(hash());
        requestInstitutionEduIDRepository.save(requestInstitutionEduID);

        String uri = basePath + "/myconext/api/sp/create-from-institution/finish";
        mailBox.sendAccountVerificationCreateFromInstitution(user, requestInstitutionEduID.getEmailHash(), uri);

        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @GetMapping("/sp/create-from-institution/poll")
    @Hidden
    public int pollCreateFromInstitution(@RequestParam("hash") String hash) {
        LOG.debug("Poll login status for create-institution-flow");

        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByHash(hash)
                .orElseThrow(() -> new ForbiddenException("Wrong hash"));
        return requestInstitutionEduID.getLoginStatus().ordinal();
    }

    @GetMapping("/sp/create-from-institution/resendMail")
    @Hidden
    public void resendMailCreateFromInstitution(@RequestParam("hash") String hash) {
        LOG.debug("Resend mail for create-institution-flow");

        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByHash(hash)
                .orElseThrow(() -> new ForbiddenException("Wrong hash"));
        if (!requestInstitutionEduID.getLoginStatus().equals(LoginStatus.NOT_LOGGED_IN)) {
            throw new ForbiddenException("User status is not NOT_LOGGED_IN, but " + requestInstitutionEduID.getLoginStatus());
        }
        String uri = basePath + "/myconext/api/sp/create-from-institution/finish";
        User user = new User(requestInstitutionEduID.getCreateInstitutionEduID(), requestInstitutionEduID.getUserInfo());
        mailBox.sendAccountVerificationCreateFromInstitution(user, requestInstitutionEduID.getEmailHash(), uri);
    }

    @GetMapping("/sp/create-from-institution/finish")
    @Hidden
    public ResponseEntity createFromInstitutionFinish(HttpServletRequest request, @RequestParam("h") String emailHash) throws UnsupportedEncodingException {
        LOG.debug("Finish create-institution-flow flow and create account");

        RequestInstitutionEduID requestInstitutionEduID = requestInstitutionEduIDRepository.findByEmailHashAndLoginStatus(emailHash, LoginStatus.NOT_LOGGED_IN)
                .orElseThrow(() -> new ForbiddenException("Wrong emailHash"));
        CreateInstitutionEduID createInstitutionEduID = requestInstitutionEduID.getCreateInstitutionEduID();
        if (createInstitutionEduID == null) {
            throw new ForbiddenException("Tampering");
        }
        Map<String, Object> userInfo = requestInstitutionEduID.getUserInfo();
        //Now we can create the User and populate the SecurityContext
        String preferredLanguage = cookieByName(request, "lang").map(Cookie::getValue).orElse("en");
        User user;
        boolean existingUser = !createInstitutionEduID.isNewUser() && StringUtils.hasText(requestInstitutionEduID.getUserId());
        if (existingUser) {
            user = userRepository.findById(requestInstitutionEduID.getUserId()).orElseThrow(() -> new UserNotFoundException("User not found"));
        } else {
            user = new User(
                    UUID.randomUUID().toString(),
                    createInstitutionEduID.getEmail(),
                    (String) userInfo.get("given_name"),
                    (String) userInfo.get("given_name"),
                    (String) userInfo.get("family_name"),
                    schacHomeOrganization,
                    preferredLanguage,
                    mijnEduIDEntityId,
                    manage);
        }
        user.setCreateFromInstitutionKey(hash());
        ResponseEntity<Object> responseEntity = saveOrUpdateLinkedAccountToUser(
                user,
                this.idpBaseRedirectUrl + "/create-from-institution-login?key=" + user.getCreateFromInstitutionKey(),
                false,
                false,
                false,
                null,
                null,
                null,
                userInfo);
        requestInstitutionEduID.setLoginStatus(LoginStatus.LOGGED_IN_SAME_DEVICE);
        requestInstitutionEduIDRepository.save(requestInstitutionEduID);

        //This is primarily for localhost development, in real environments the login will be done by Shibboleth filter
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null,
                user.getAuthorities());
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(authentication);
        HttpSession session = request.getSession();
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);

        return responseEntity;
    }

    @GetMapping("/sp/oidc/link")
    @Operation(summary = "Start link account flow",
            description = "Start the link account flow for the current user." +
                    "<br/>After the account has been linked the user is redirect to one the following URL's:" +
                    "<ul>" +
                    "<li>Success: <a href=\"\">https://login.{environment}.eduid.nl/client/mobile/account-linked</a></li>" +
                    "<li>Failure, EPPN already linked: <a href=\"\">https://login.{environment}.eduid.nl/client/mobile/eppn-already-linked?email=jdoe%40example.com</a></li>" +
                    "<li>Failure, session expired: <a href=\"\">https://login.{environment}.eduid.nl/client/mobile/expired</a></li>" +
                    "</ul>",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Url for authentication", useReturnTypeSchema = true,
                            content = {@Content(schema = @Schema(implementation = AuthorizationURL.class), examples =
                                    {@ExampleObject(value =
                                            "{\"url\":\"https://connect.test2.surfconext.nl/oidc/authorize?scope=openid&response_type=code&redirect_uri=https://mijn.test2.eduid.nl/myconext/api/sp/oidc/redirect&state=%242a%2410%249cyC3mjeJW0ljb%2FmPAGj0O4DVXz9LPw5U%2Fthl110BVYWFpMhjwKyK&prompt=login&client_id=myconext.ala.eduid\"}")})})}
    )
    public ResponseEntity<AuthorizationURL> startSPLinkAccountFlow(Authentication authentication) throws UnsupportedEncodingException {
        User user = userFromAuthentication(authentication);

        LOG.info(String.format("Start link account flow for user %s", user.getEmail()));

        String state;
        String redirectUri;
        if (isMobileRequest(authentication)) {
            MobileLinkAccountRequest mobileLinkAccountRequest = this.mobileLinkAccountRequestRepository.save(new MobileLinkAccountRequest(hash(), user.getId()));
            state = mobileLinkAccountRequest.getHash();
            redirectUri = this.mobileFlowRedirectUri;
        } else {
            state = passwordEncoder.encode(user.getUid());
            redirectUri = this.spFlowRedirectUri;
        }
        UriComponents uriComponents = doStartLinkAccountFlow(state, redirectUri, true, myConextSpEntityId);
        return ResponseEntity.ok(new AuthorizationURL(uriComponents.toUriString()));
    }

    @GetMapping("/sp/idin/issuers")
    @Operation(summary = "All verify issuers",
            description = "All verify issuers to build the select Bank page for ID verificatin")
    public ResponseEntity<List<VerifyIssuer>> issuers() {
        LOG.debug("Retrieve IDIN issuers");
        return ResponseEntity.ok(issuers);
    }

    @GetMapping("/sp/verify/link")
    @Operation(summary = "Start verify ID flow for signicat from SP flow",
            description = "Start the verify ID flow for the current user." +
                    "<br/>After the account has been linked the user is redirect to one the following URL's:" +
                    "<ul>" +
                    "<li>Success: <a href=\"\">https://login.{environment}.eduid.nl/client/mobile/verify-account-linked</a></li>" +
                    "<li>Failure, something went wrong: <a href=\"\">https://login.{environment}.eduid.nl/client/mobile/verify-error</a></li>" +
                    "</ul>",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Url for authentication", useReturnTypeSchema = true,
                            content = {@Content(schema = @Schema(implementation = AuthorizationURL.class), examples =
                                    {@ExampleObject(value =
                                            "{\"url\":\"https://validate.test.eduid.nl/broker/sp/oidc/authenticate?scope=openid&response_type=code&redirect_uri=https://mijn.test2.eduid.nl/myconext/api/sp/verify/redirect&state=%242a%2410%249cyC3mjeJW0ljb%2FmPAGj0O4DVXz9LPw5U%2Fthl110BVYWFpMhjwKyK&prompt=login&client_id=myconext.ala.eduid\"}")})})}
    )
    public ResponseEntity<AuthorizationURL> startSPVerifyIDLinkAccountFlow(Authentication authentication,
                                                                       @RequestParam("idpScoping") IdpScoping idpScoping,
                                                                       @RequestParam(value = "bankId", required = false) String bankId) {
        User user = userFromAuthentication(authentication);

        LOG.info(String.format("Start verify account flow for user %s for flow %s", user.getEmail(), idpScoping));

        String stateIdentifier;
        String redirectUri;
        if (isMobileRequest(authentication)) {
            MobileLinkAccountRequest mobileLinkAccountRequest = this.mobileLinkAccountRequestRepository.save(new MobileLinkAccountRequest(hash(), user.getId()));
            stateIdentifier = mobileLinkAccountRequest.getHash();
            redirectUri = this.mobileVerifyRedirectUri;
        } else {
            stateIdentifier = passwordEncoder.encode(user.getUid());
            redirectUri = this.spVerifyRedirectUri;
        }
        VerifyIssuer verifyIssuer = idpScoping.equals(IdpScoping.idin) ? this.issuers.stream()
                .filter(issuer -> issuer.getId().equals(bankId)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown verify issuer: " + bankId)) :
                new VerifyIssuer(IdpScoping.eherkenning.name(), IdpScoping.eherkenning.name());
        VerifyState verifyState = new VerifyState(stateIdentifier, idpScoping, verifyIssuer);
        String state = attributeMapper.serializeToBase64(verifyState);

        Map<String, String> params = new HashMap<>();

        params.put("client_id", verifyClientId);
        params.put("response_type", "code");
        params.put("scope", String.format("openid dateofbirth name idp_scoping:%s%s",
                idpScoping.name(),
                StringUtils.hasText(bankId) ? " signicat:param:idin_idp:" + bankId : ""));
        params.put("redirect_uri", redirectUri);
        params.put("state", state);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.verifyBaseUri + "/broker/sp/oidc/authenticate");
        params.forEach(builder::queryParam);
        UriComponents uriComponents = builder.build();
        return ResponseEntity.ok(new AuthorizationURL(uriComponents.toUriString()));
    }

    @GetMapping({"/sp/verify/redirect"})
    @Hidden
    public ResponseEntity spVerifyIDRedirect(Authentication authentication,
                                             @RequestParam(value = "code", required = false) String code,
                                             @RequestParam(value = "state", required = false) String state,
                                             @RequestParam(value = "error", required = false) String error,
                                             @RequestParam(value = "error_description", required = false) String errorDescription) {
        User user = userFromAuthentication(authentication);

        if (!StringUtils.hasText(code) || !StringUtils.hasText(state)) {
            URI location = URI.create(String.format("%s/external-account-linked-error?error=%s&error_description=%s",
                            spRedirectUrl,
                            StringUtils.hasText(error) ? URLEncoder.encode(error, Charset.defaultCharset()) : "",
                            StringUtils.hasText(errorDescription) ? URLEncoder.encode(errorDescription, Charset.defaultCharset()) : "Unexpected+error+occurred"
                    )
            );
            return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
        }

        VerifyState verifyState = attributeMapper.serializeFromBase64(state);

        LOG.info(String.format("In SP verify ID redirect link account for user %s and party %s", user.getEmail(), verifyState.getIdpScoping()));

        if (!passwordEncoder.matches(user.getUid(), verifyState.getStateIdentifier())) {
            throw new ForbiddenException("Non matching user");
        }
        HttpHeaders headers = getHttpHeaders();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", verifyClientId);
        map.add("client_secret", verifySecret);
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", spVerifyRedirectUri);
        map.add("scope", "openid");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ParameterizedTypeReference<Map<String, Object>> parameterizedTypeReference = new ParameterizedTypeReference<>() {
        };
        Map<String, Object> body = restTemplate.exchange(verifyBaseUri + "/broker/sp/oidc/token",
                HttpMethod.POST, request, parameterizedTypeReference).getBody();

        MultiValueMap<String, String>  tokenMap = new LinkedMultiValueMap<>();
        tokenMap.add("access_token", (String) body.get("access_token"));

        request = new HttpEntity<>(tokenMap, headers);
        Map<String, Object> attributes = restTemplate.exchange(verifyBaseUri + "/broker/sp/oidc/userinfo", HttpMethod.POST, request, parameterizedTypeReference).getBody();

        ExternalLinkedAccount externalLinkedAccount = attributeMapper.externalLinkedAccountFromAttributes(attributes, verifyState);
        Optional<User> optionalUser = userRepository.findByExternalLinkedAccounts_SubjectId(externalLinkedAccount.getSubjectId());
        if (optionalUser.isPresent() && !user.getId().equals(optionalUser.get().getId())) {
            //Not allowed to link an external linked account which identity is already linked to another user
            LOG.warn(String.format("SP redirect for external account linking: subject %s already linked to %s for party %s",
                    externalLinkedAccount.getSubjectId(),
                    user.getEmail(),
                    verifyState.getIdpScoping()));
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(spRedirectUrl + "/subject-already-linked?idp_scoping"))
                    .build();
        }
        List<ExternalLinkedAccount> externalLinkedAccounts = user.getExternalLinkedAccounts();
        //We only allow one ExternalLinkedAccount - for now
        externalLinkedAccounts.clear();
        externalLinkedAccounts.add(externalLinkedAccount);

        if (StringUtils.hasText(externalLinkedAccount.getFirstName()) && IdpScoping.eherkenning.equals(externalLinkedAccount.getIdpScoping()) ) {
            user.setGivenName(externalLinkedAccount.getFirstName());
        }
        if (StringUtils.hasText(externalLinkedAccount.getInitials()) && IdpScoping.idin.equals(externalLinkedAccount.getIdpScoping()) ) {
            user.setGivenName(externalLinkedAccount.getInitials());
        }
        if (StringUtils.hasText(externalLinkedAccount.getLegalLastName())) {
            user.setFamilyName(externalLinkedAccount.getLegalLastName());
        }
        if (externalLinkedAccount.getDateOfBirth() != null) {
            user.setDateOfBirth(externalLinkedAccount.getDateOfBirth());
        }
        userRepository.save(user);

        URI location = URI.create(spRedirectUrl + "/personal?verify=" + externalLinkedAccount.getIdpScoping());
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
    }

    @GetMapping("/idp/verify/link/{id}")
    @Hidden
    public ResponseEntity<AuthorizationURL> startIdPVerifyIDLinkAccountFlow(@PathVariable("id") String id,
                                                                            @RequestParam("idpScoping") IdpScoping idpScoping,
                                                                            @RequestParam(value = "bankId", required = false) String bankId) {
        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(id);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(this.idpBaseRedirectUrl + "/expired")).build();
        }
        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();

        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        LOG.info(String.format("Start Idp verify account flow for user %s and party %s", user.getEmail(), idpScoping));

        VerifyIssuer verifyIssuer = idpScoping.equals(IdpScoping.idin) ? this.issuers.stream()
                .filter(issuer -> issuer.getId().equals(bankId)).findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown verify issuer: " + bankId)) :
                new VerifyIssuer(IdpScoping.eherkenning.name(), IdpScoping.eherkenning.name());
        String stateIdentifier = String.format("id=%s&user_uid=%s", id, passwordEncoder.encode(user.getUid()));
        VerifyState verifyState = new VerifyState(stateIdentifier, idpScoping, verifyIssuer);
        String state = attributeMapper.serializeToBase64(verifyState);

        Map<String, String> params = new HashMap<>();

        params.put("client_id", verifyClientId);
        params.put("response_type", "code");
        params.put("scope", String.format("openid dateofbirth name idp_scoping:%s%s",
                idpScoping.name(),
                StringUtils.hasText(bankId) ? " signicat:param:idin_idp:" + bankId : ""));
        params.put("redirect_uri", this.idpVerifyRedirectUri);
        params.put("state", state);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(this.verifyBaseUri + "/broker/sp/oidc/authenticate");
        params.forEach(builder::queryParam);
        UriComponents uriComponents = builder.build();
        return ResponseEntity.status(HttpStatus.FOUND).location(uriComponents.toUri()).build();
    }

    @GetMapping({"/idp/verify/redirect"})
    @Hidden
    public ResponseEntity idpVerifyIDRedirect(@RequestParam(value = "code", required = false) String code,
                                              @RequestParam(value = "state", required = false) String state,
                                              @RequestParam(value = "error", required = false) String error,
                                              @RequestParam(value = "error_description", required = false) String errorDescription) {
        LOG.debug("In IDP verify ID redirect link account");

        if (!StringUtils.hasText(code) || !StringUtils.hasText(state)) {
            URI location = URI.create(String.format("%s/external-account-linked-error?error=%s&error_description=%s",
                            this.idpBaseRedirectUrl,
                            StringUtils.hasText(error) ? URLEncoder.encode(error, Charset.defaultCharset()) : "",
                            StringUtils.hasText(errorDescription) ? URLEncoder.encode(errorDescription, Charset.defaultCharset()) : "Unexpected+error+occurred"
                    )
            );
            LOG.warn("Error response from trusted party for external account linking");
            return ResponseEntity.status(HttpStatus.FOUND).location(location).build();
        }

        VerifyState verifyState = attributeMapper.serializeFromBase64(state);

        String httpUrl = "http://localhost?" + verifyState.getStateIdentifier();
        MultiValueMap<String, String> params = UriComponentsBuilder.fromHttpUrl(httpUrl).build().getQueryParams();
        String id = params.getFirst("id");
        String encodedUserUid = params.getFirst("user_uid");

        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(id);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(this.idpBaseRedirectUrl + "/expired")).build();
        }
        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();

        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        LOG.info(String.format("In IDP verify ID redirect link account for user %s and party %s", user.getEmail(), verifyState.getIdpScoping()));

        if (!passwordEncoder.matches(user.getUid(), encodedUserUid)) {
            throw new ForbiddenException("Non matching user");
        }
        HttpHeaders headers = getHttpHeaders();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", verifyClientId);
        map.add("client_secret", verifySecret);
        map.add("code", code);
        map.add("grant_type", "authorization_code");
        map.add("redirect_uri", spVerifyRedirectUri);
        map.add("scope", "openid");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        ParameterizedTypeReference<Map<String, Object>> parameterizedTypeReference = new ParameterizedTypeReference<>() {
        };
        Map<String, Object> body = restTemplate.exchange(verifyBaseUri + "/broker/sp/oidc/token",
                HttpMethod.POST, request, parameterizedTypeReference).getBody();

        MultiValueMap<String, String> tokenMap = new LinkedMultiValueMap<>();
        tokenMap.add("access_token", (String) body.get("access_token"));

        request = new HttpEntity<>(tokenMap, headers);
        Map<String, Object> attributes = restTemplate.exchange(verifyBaseUri + "/broker/sp/oidc/userinfo", HttpMethod.POST, request, parameterizedTypeReference).getBody();

        ExternalLinkedAccount externalLinkedAccount = attributeMapper.externalLinkedAccountFromAttributes(attributes, verifyState);
        Optional<User> optionalUser = userRepository.findByExternalLinkedAccounts_SubjectId(externalLinkedAccount.getSubjectId());
        if (optionalUser.isPresent() && !user.getId().equals(optionalUser.get().getId())) {
            //Not allowed to link an external linked account which identity is already linked to another user
            LOG.warn(String.format("Subject %s already linked to user %s", externalLinkedAccount.getSubjectId(), user.getEmail()));

            String subjectAlreadyLinkedRequiredUri = this.idpBaseRedirectUrl + "/subject-already-linked/" +
                    samlAuthenticationRequest.getId() +
                    "?h=" + samlAuthenticationRequest.getHash() +
                    "&redirect=" + URLEncoder.encode(this.magicLinkUrl, Charset.defaultCharset());
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(subjectAlreadyLinkedRequiredUri)).build();
        }
        List<ExternalLinkedAccount> externalLinkedAccounts = user.getExternalLinkedAccounts();
        //We only allow one ExternalLinkedAccount - for now
        externalLinkedAccounts.clear();
        externalLinkedAccounts.add(externalLinkedAccount);

        LOG.info(String.format("New external linked account %s for user %s", externalLinkedAccount, user.getEmail()));

        if (StringUtils.hasText(externalLinkedAccount.getFirstName()) && IdpScoping.eherkenning.equals(externalLinkedAccount.getIdpScoping())) {
            user.setGivenName(externalLinkedAccount.getFirstName());
        }
        if (StringUtils.hasText(externalLinkedAccount.getLegalLastName())) {
            user.setFamilyName(externalLinkedAccount.getLegalLastName());
        }
        if (externalLinkedAccount.getDateOfBirth() != null) {
            user.setDateOfBirth(externalLinkedAccount.getDateOfBirth());
        }
        userRepository.save(user);

        String location = this.magicLinkUrl + "?h=" + samlAuthenticationRequest.getHash();

        samlAuthenticationRequest.setSteppedUp(StepUpStatus.IN_STEP_UP);
        authenticationRequestRepository.save(samlAuthenticationRequest);

        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(location)).build();
    }

    private UriComponents doStartLinkAccountFlow(String state, String redirectUri, boolean forceAuth, String requesterEntityId) {
        Map<String, String> params = new HashMap<>();

        params.put("client_id", clientId);
        params.put("response_type", "code");
        params.put("scope", "openid");
        params.put("redirect_uri", redirectUri);
        params.put("state", URLEncoder.encode(state, StandardCharsets.UTF_8));
        if (forceAuth) {
            params.put("prompt", "login");
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(oidcBaseUrl + "/oidc/authorize");
        params.forEach(builder::queryParam);
        return builder.build();
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    @GetMapping("/sp/oidc/redirect")
    @Hidden
    public ResponseEntity spFlowRedirect(Authentication authentication, @RequestParam("code") String code, @RequestParam("state") String state) throws UnsupportedEncodingException {
        LOG.debug("");

        User user = userFromAuthentication(authentication);

        LOG.info(String.format("In SP redirect link account for user %s", user.getEmail()));

        if (!passwordEncoder.matches(user.getUid(), URLDecoder.decode(state, StandardCharsets.UTF_8))) {
            throw new ForbiddenException("Non matching user");
        }

        return doRedirect(code, user, this.spFlowRedirectUri, this.spRedirectUrl + "/personal",
                false, false, true, null, null,
                this.spRedirectUrl + "/eppn-already-linked");
    }

    @GetMapping("/mobile/oidc/redirect")
    @Hidden
    public ResponseEntity mobileFlowRedirect(@RequestParam("code") String code, @RequestParam("state") String state) throws UnsupportedEncodingException {

        String decodedState = URLDecoder.decode(state, StandardCharsets.UTF_8);
        Optional<MobileLinkAccountRequest> optionalMobileLinkAccountRequest = this.mobileLinkAccountRequestRepository.findByHash(decodedState);
        if (!optionalMobileLinkAccountRequest.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(this.idpBaseRedirectUrl + "/client/mobile/expired")).build();
        }

        MobileLinkAccountRequest mobileLinkAccountRequest = optionalMobileLinkAccountRequest.get();
        String userId = mobileLinkAccountRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        LOG.info(String.format("In Mobile redirect link account for user %s", user.getEmail()));

        this.mobileLinkAccountRequestRepository.delete(mobileLinkAccountRequest);

        return doRedirect(code, user, this.mobileFlowRedirectUri, this.idpBaseRedirectUrl + "/client/mobile/account-linked",
                false, false, true, null, null,
                this.idpBaseRedirectUrl + "/client/mobile/eppn-already-linked");
    }

    @GetMapping("/idp/oidc/redirect")
    @Hidden
    public ResponseEntity idpFlowRedirect(@RequestParam("code") String code, @RequestParam("state") String state) throws UnsupportedEncodingException {
        String decodedState = URLDecoder.decode(state, StandardCharsets.UTF_8);
        MultiValueMap<String, String> params = UriComponentsBuilder.fromHttpUrl("http://localhost?" + decodedState).build().getQueryParams();
        String id = params.getFirst("id");
        String encodedUserUid = params.getFirst("user_uid");

        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(id);
        if (!optionalSamlAuthenticationRequest.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(this.idpBaseRedirectUrl + "/expired")).build();
        }
        SamlAuthenticationRequest samlAuthenticationRequest = optionalSamlAuthenticationRequest.get();
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        LOG.info(String.format("In IdP oidc redirect link account for user %s", user.getEmail()));

        if (!passwordEncoder.matches(user.getUid(), encodedUserUid)) {
            throw new ForbiddenException("Non matching user");
        }

        boolean validateNames = samlAuthenticationRequest.getAuthenticationContextClassReferences().contains(ACR.VALIDATE_NAMES);
        boolean studentAffiliationRequired = samlAuthenticationRequest.getAuthenticationContextClassReferences().contains(ACR.AFFILIATION_STUDENT);

        String location = this.magicLinkUrl + "?h=" + samlAuthenticationRequest.getHash();

        String charSet = Charset.defaultCharset().name();

        String idpStudentAffiliationRequiredUri = this.idpBaseRedirectUrl + "/affiliation-missing/" +
                samlAuthenticationRequest.getId() +
                "?h=" + samlAuthenticationRequest.getHash() +
                "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet);

        String idpValidNamesRequiredUri = this.idpBaseRedirectUrl + "/valid-name-missing/" +
                samlAuthenticationRequest.getId() +
                "?h=" + samlAuthenticationRequest.getHash() +
                "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet);

        String eppnAlreadyLinkedRequiredUri = this.idpBaseRedirectUrl + "/eppn-already-linked/" +
                samlAuthenticationRequest.getId() +
                "?h=" + samlAuthenticationRequest.getHash() +
                "&redirect=" + URLEncoder.encode(this.magicLinkUrl, charSet);

        ResponseEntity redirect = doRedirect(code, user, this.idpFlowRedirectUri, location, validateNames, studentAffiliationRequired, false,
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
                                      boolean appendEPPNQueryParam,
                                      String idpStudentAffiliationRequiredUri,
                                      String idpValidNamesRequiredUri,
                                      String eppnAlreadyLinkedRequiredUri) throws UnsupportedEncodingException {
        Map<String, Object> body = requestUserInfo(code, oidcRedirectUri);

        LOG.info(String.format("In redirect link account for user %s with user info %s", user.getEmail(), body));

        return saveOrUpdateLinkedAccountToUser(user, clientRedirectUri, validateNames, studentAffiliationRequired,
                appendEPPNQueryParam, idpStudentAffiliationRequiredUri, idpValidNamesRequiredUri, eppnAlreadyLinkedRequiredUri, body);
    }

    private ResponseEntity<Object> saveOrUpdateLinkedAccountToUser(User user,
                                                                   String clientRedirectUri,
                                                                   boolean validateNames,
                                                                   boolean studentAffiliationRequired,
                                                                   boolean appendEPPNQueryParam,
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
            Date expiresAt = Date.from(new Date().toInstant().plus(this.removalValidatedDurationDays, ChronoUnit.DAYS));
            List<LinkedAccount> linkedAccounts = user.getLinkedAccounts();
            Optional<LinkedAccount> optionalLinkedAccount = linkedAccounts.stream()
                    .filter(linkedAccount -> linkedAccount.getEduPersonPrincipalName().equalsIgnoreCase(eppn))
                    .findFirst();
            if (optionalLinkedAccount.isPresent()) {
                optionalLinkedAccount.get().updateExpiresIn(institutionIdentifier, eppn, subjectId, givenName, familyName, affiliations, expiresAt);
            } else {
                Optional<ResponseEntity<Object>> eppnAlreadyLinkedOptional = checkEppnAlreadyLinked(eppnAlreadyLinkedRequiredUri, eppn);
                if (eppnAlreadyLinkedOptional.isPresent()) {
                    return eppnAlreadyLinkedOptional.get();
                }
                linkedAccounts.add(
                        new LinkedAccount(institutionIdentifier, schacHomeOrganization, eppn, subjectId, givenName, familyName, affiliations, linkedAccounts.isEmpty(),
                                new Date(), expiresAt));
            }
            if (linkedAccounts.size() == 1) {
                if (StringUtils.hasText(givenName)) {
                    user.setGivenName(givenName);
                }
                if (StringUtils.hasText(familyName)) {
                    user.setFamilyName(familyName);
                }
            }
            String eppnValue = StringUtils.hasText(eppn) ? String.format("eppn %s", eppn) : "NO eppn";

            logWithContext(user, "add", "linked_accounts", LOG, String.format("Account link with EPPN %s for institution %s with the affiliations %s",
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

        if (appendEPPNQueryParam) {
            String appender = clientRedirectUri.contains("?") ? "&" : "?";
            clientRedirectUri += appender + "institution=" + URLEncoder.encode(eppn, Charset.defaultCharset());
        }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(clientRedirectUri)).build();
    }

    private Optional<ResponseEntity<Object>> checkEppnAlreadyLinked(String eppnAlreadyLinkedRequiredUri, String eppn) throws UnsupportedEncodingException {
        //Ensure that an institution account is only be linked to 1 eduID, but only when an eppn is provided for the linked account
        if (StringUtils.hasText(eppn)) {
            List<User> optionalUsers = userRepository.findByLinkedAccounts_EduPersonPrincipalName(eppn);
            if (!optionalUsers.isEmpty()) {
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

        MultiValueMap<String, String> accessTokenMap = new LinkedMultiValueMap<>(Map.of("access_token", List.of((String) body.get("access_token"))));
        request = new HttpEntity<>(accessTokenMap, headers);
        return restTemplate.exchange(oidcBaseUrl + "/oidc/userinfo", HttpMethod.POST, request, parameterizedTypeReference).getBody();
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

    @Override
    public UserRepository getUserRepository() {
        return userRepository;
    }
}
