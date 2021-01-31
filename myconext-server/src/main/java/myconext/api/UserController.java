package myconext.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import myconext.cron.IdPMetaDataResolver;
import myconext.exceptions.ExpiredAuthenticationException;
import myconext.exceptions.ForbiddenException;
import myconext.exceptions.UserNotFoundException;
import myconext.mail.MailBox;
import myconext.manage.ServiceProviderResolver;
import myconext.model.*;
import myconext.oidcng.OpenIDConnect;
import myconext.repository.*;
import myconext.security.EmailDomainGuard;
import myconext.security.EmailGuessingPrevention;
import myconext.webauthn.UserCredentialRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

import static myconext.log.MDCContext.logLoginWithContext;
import static myconext.log.MDCContext.logWithContext;
import static myconext.security.CookieResolver.cookieByName;

@RestController
@RequestMapping("/myconext/api")
public class UserController {

    private static final Log LOG = LogFactory.getLog(UserController.class);

    private final UserRepository userRepository;
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final MailBox mailBox;
    private final ServiceProviderResolver serviceProviderResolver;
    private final OpenIDConnect openIDConnect;
    private final String magicLinkUrl;
    private final String schacHomeOrganization;
    private final String webAuthnSpRedirectUrl;
    private final String idpBaseUrl;
    private final RelyingParty relyingParty;
    private final UserCredentialRepository userCredentialRepository;
    private final ChallengeRepository challengeRepository;
    private final PasswordForgottenHashRepository passwordForgottenHashRepository;
    private final ChangeEmailHashRepository changeEmailHashRepository;

    private final static SecureRandom random = new SecureRandom();
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(-1, random);
    private final EmailGuessingPrevention emailGuessingPreventor;
    private final EmailDomainGuard emailDomainGuard;
    private final IdPMetaDataResolver idPMetaDataResolver;
    private final String spBaseUrl;
    private final ObjectMapper objectMapper;

    public UserController(UserRepository userRepository,
                          UserCredentialRepository userCredentialRepository,
                          ChallengeRepository challengeRepository,
                          PasswordForgottenHashRepository passwordForgottenHashRepository,
                          ChangeEmailHashRepository changeEmailHashRepository,
                          AuthenticationRequestRepository authenticationRequestRepository,
                          MailBox mailBox,
                          ServiceProviderResolver serviceProviderResolver,
                          OpenIDConnect openIDConnect,
                          IdPMetaDataResolver idPMetaDataResolver,
                          EmailDomainGuard emailDomainGuard,
                          @Qualifier("jsonMapper") ObjectMapper objectMapper,
                          @Value("${email.magic-link-url}") String magicLinkUrl,
                          @Value("${schac_home_organization}") String schacHomeOrganization,
                          @Value("${email_guessing_sleep_millis}") int emailGuessingSleepMillis,
                          @Value("${sp_redirect_url}") String spBaseUrl,
                          @Value("${idp_redirect_url}") String idpBaseUrl,
                          @Value("${rp_origin}") String rpOrigin,
                          @Value("${rp_id}") String rpId) {
        this.userRepository = userRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.challengeRepository = challengeRepository;
        this.passwordForgottenHashRepository = passwordForgottenHashRepository;
        this.changeEmailHashRepository = changeEmailHashRepository;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.mailBox = mailBox;
        this.serviceProviderResolver = serviceProviderResolver;
        this.openIDConnect = openIDConnect;
        this.idPMetaDataResolver = idPMetaDataResolver;
        this.emailDomainGuard = emailDomainGuard;
        this.objectMapper = objectMapper;
        this.magicLinkUrl = magicLinkUrl;
        this.schacHomeOrganization = schacHomeOrganization;
        this.idpBaseUrl = idpBaseUrl;
        this.spBaseUrl = spBaseUrl;
        this.webAuthnSpRedirectUrl = String.format("%s/webauthn", spBaseUrl);
        this.relyingParty = relyingParty(rpId, rpOrigin);
        this.emailGuessingPreventor = new EmailGuessingPrevention(emailGuessingSleepMillis);
    }

    @GetMapping("/idp/email/domain/institutional")
    public Set<String> institutionalDomains() {
        return this.idPMetaDataResolver.getDomainNames();
    }

    @GetMapping("/idp/email/domain/allowed")
    public Set<String> allowedDomains() {
        return this.emailDomainGuard.getAllowedDomains();
    }

    @PostMapping("/idp/magic_link_request")
    public ResponseEntity newMagicLinkRequest(HttpServletRequest request, @Valid @RequestBody MagicLinkRequest magicLinkRequest) {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(magicLinkRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);

        User user = magicLinkRequest.getUser();

        String email = user.getEmail();
        emailDomainGuard.enforceIsAllowed(email);
        emailGuessingPreventor.potentialUserEmailGuess();

        Optional<User> optionalUser = userRepository.findUserByEmailIgnoreCase(emailGuessingPreventor.sanitizeEmail(email));
        if (optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("status", HttpStatus.CONFLICT.value()));
        }
        String preferredLanguage = LocaleContextHolder.getLocale().getLanguage();
        //prevent not-wanted attributes in the database
        String requesterEntityId = samlAuthenticationRequest.getRequesterEntityId();
        String schacHomeOrganization = this.emailDomainGuard.schacHomeOrganizationByDomain(this.schacHomeOrganization, email);

        User userToSave = new User(UUID.randomUUID().toString(), email, user.getGivenName(),
                user.getFamilyName(), schacHomeOrganization, preferredLanguage, requesterEntityId, serviceProviderResolver);
        userToSave = userRepository.save(userToSave);

        return this.doMagicLink(userToSave, samlAuthenticationRequest, magicLinkRequest.isRememberMe(), false, request);
    }

    @PutMapping("/idp/magic_link_request")
    public ResponseEntity magicLinkRequest(HttpServletRequest request, @Valid @RequestBody MagicLinkRequest magicLinkRequest) {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(magicLinkRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);

        User providedUser = magicLinkRequest.getUser();

        String email = providedUser.getEmail();
        emailDomainGuard.enforceIsAllowed(email);
        emailGuessingPreventor.potentialUserEmailGuess();

        Optional<User> optionalUser = findUserStoreLanguage(email);
        if (!optionalUser.isPresent()) {
            return return404();
        }
        User user = optionalUser.get();
        String requesterEntityId = samlAuthenticationRequest.getRequesterEntityId();

        logWithContext(user, "update", "user", LOG, "Updating user " + user.getEmail());
        user.computeEduIdForServiceProviderIfAbsent(requesterEntityId, serviceProviderResolver);
        userRepository.save(user);

        if (magicLinkRequest.isUsePassword()) {
            if (!passwordEncoder.matches(providedUser.getPassword(), user.getPassword())) {
                logLoginWithContext(user, "password", false, LOG, "Bad attempt to login with password");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Collections.singletonMap("status", HttpStatus.FORBIDDEN.value()));
            }
            logLoginWithContext(user, "password", true, LOG, "Successfully logged in with password");
            LOG.info("Successfully logged in with password");
        }
        return doMagicLink(user, samlAuthenticationRequest, magicLinkRequest.isRememberMe(), magicLinkRequest.isUsePassword(), request);
    }

    @GetMapping("/idp/resend_magic_link_request")
    public ResponseEntity resendMagicLinkRequest(HttpServletRequest request, @RequestParam("id") String authenticationRequestId) {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(authenticationRequestId)
                .orElseThrow(ExpiredAuthenticationException::new);
        User user = userRepository.findById(samlAuthenticationRequest.getUserId()).orElseThrow(UserNotFoundException::new);
        if (user.isNewUser()) {
            mailBox.sendAccountVerification(user, samlAuthenticationRequest.getHash());
        } else {
            String serviceName = getServiceName(request, samlAuthenticationRequest);
            mailBox.sendMagicLink(user, samlAuthenticationRequest.getHash(), serviceName);
        }
        return ResponseEntity.ok(true);
    }

    private String getServiceName(HttpServletRequest request, SamlAuthenticationRequest samlAuthenticationRequest) {
        String lang = cookieByName(request, "lang").map(cookie -> cookie.getValue()).orElse("en");
        Optional<ServiceProvider> optionalServiceProvider = serviceProviderResolver.resolve(samlAuthenticationRequest.getRequesterEntityId());
        String serviceName = optionalServiceProvider.map(serviceProvider -> lang.equals("en") ? serviceProvider.getName() : serviceProvider.getNameNl())
                .orElse(samlAuthenticationRequest.getRequesterEntityId());
        return serviceName;
    }

    @GetMapping("/idp/security/success")
    public int successfullyLoggedIn(@RequestParam("id") String id) {
        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findById(id);
        return optionalSamlAuthenticationRequest.map(samlAuthenticationRequest -> samlAuthenticationRequest.getLoginStatus().ordinal()).orElse(LoginStatus.NOT_LOGGED_IN.ordinal());
    }

    @GetMapping(value = {"/sp/me", "sp/migrate/merge", "sp/migrate/proceed"})
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        User user = userRepository.findOneUserByEmailIgnoreCase(((User) authentication.getPrincipal()).getEmail());
        return userResponseRememberMe(user);
    }

    @DeleteMapping("/sp/forget")
    public ResponseEntity<Long> forgetMe(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        String userId = user.getId();
        Long count = authenticationRequestRepository.deleteByUserId(userId);
        logWithContext(user, "delete", "rememberme", LOG, "Do not remember user anymore");
        return ResponseEntity.ok(count);
    }

    @PutMapping("/sp/update")
    public ResponseEntity updateUserProfile(Authentication authentication, @RequestBody User deltaUser) {
        User user = verifyAndFetchUser(authentication, deltaUser);

        user.setFamilyName(deltaUser.getFamilyName());
        user.setGivenName(deltaUser.getGivenName());
        user.validate();

        userRepository.save(user);
        logWithContext(user, "update", "name", LOG, "Update user profile");
        authenticationRequestRepository.deleteByUserId(user.getId());
        return returnUserResponse(user);
    }

    @PutMapping("/sp/email")
    public ResponseEntity updateEmail(Authentication authentication, @RequestBody User deltaUser) {
        User user = userFromAuthentication(authentication);
        changeEmailHashRepository.deleteByUserId(user.getId());

        String hashValue = hash();
        String newEmail = deltaUser.getEmail();
        Optional<User> optionalUser = userRepository.findUserByEmailIgnoreCase(emailGuessingPreventor.sanitizeEmail(newEmail));
        if (optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("status", HttpStatus.CONFLICT.value()));
        }

        changeEmailHashRepository.save(new ChangeEmailHash(user, newEmail, hashValue));
        logWithContext(user, "update", "update-email", LOG, "Send update email mail");

        mailBox.sendUpdateEmail(user, newEmail, hashValue);
        authenticationRequestRepository.deleteByUserId(user.getId());
        return returnUserResponse(user);
    }

    @GetMapping("/sp/confirm-email")
    public ResponseEntity confirmUpdateEmail(Authentication authentication, @RequestParam(value = "h") String hash) {
        User user = userFromAuthentication(authentication);
        ChangeEmailHash changeEmailHash = changeEmailHashRepository.findByHashAndUserId(hash, user.getId())
                .orElseThrow(() -> new ForbiddenException("wrong_hash"));

        user.setEmail(changeEmailHash.getNewEmail());
        userRepository.save(user);
        authenticationRequestRepository.deleteByUserId(user.getId());
        return returnUserResponse(user);
    }

    private ResponseEntity<UserResponse> returnUserResponse(User user) {
        return ResponseEntity.status(201).body(new UserResponse(user, convertEduIdPerServiceProvider(user), false));
    }

    @PutMapping("/sp/security")
    public ResponseEntity updateUserSecurity(Authentication authentication, @RequestBody UpdateUserSecurityRequest updateUserRequest) {
        User deltaUser = userRepository.findById(updateUserRequest.getUserId()).orElseThrow(UserNotFoundException::new);
        User user = verifyAndFetchUser(authentication, deltaUser);

        String password = user.getPassword();

        boolean existingPassword = StringUtils.hasText(password);
        String currentPasswordFromUser = updateUserRequest.getCurrentPassword();
        boolean passwordMatches = currentPasswordFromUser != null && passwordEncoder.matches(currentPasswordFromUser, password);
        boolean forgottenPassword = user.isForgottenPassword();

        if (existingPassword && !passwordMatches && !forgottenPassword) {
            throw new ForbiddenException("no_match");
        }

        if (forgottenPassword && !passwordMatches) {
            passwordForgottenHashRepository
                    .findByHashAndUserId(updateUserRequest.getHash(), user.getId())
                    .orElseThrow(() -> new ForbiddenException("wrong_hash"));
        }
        user.encryptPassword(updateUserRequest.getNewPassword(), passwordEncoder);
        user.setForgottenPassword(false);

        userRepository.save(user);
        passwordForgottenHashRepository.deleteByUserId(user.getId());

        String action = existingPassword ? "update" : "add";
        logWithContext(user, action, "password", LOG, action + " password");
        authenticationRequestRepository.deleteByUserId(user.getId());
        return returnUserResponse(user);
    }

    @PutMapping("/sp/forgot-password")
    public ResponseEntity forgotPassword(Authentication authentication) {
        User user = userFromAuthentication(authentication);
        passwordForgottenHashRepository.deleteByUserId(user.getId());

        user.setForgottenPassword(true);
        userRepository.save(user);

        String hashValue = hash();
        passwordForgottenHashRepository.save(new PasswordForgottenHash(user, hashValue));

        logWithContext(user, "update", "forgot-password", LOG, "Send password forgotten mail");

        mailBox.sendForgotPassword(user, hashValue);
        authenticationRequestRepository.deleteByUserId(user.getId());
        return returnUserResponse(user);
    }


    @PutMapping("/sp/institution")
    public ResponseEntity removeUserLinkedAccounts(Authentication authentication, @RequestBody LinkedAccount linkedAccount) {
        User user = userFromAuthentication(authentication);

        List<LinkedAccount> linkedAccounts = user.getLinkedAccounts().stream()
                .filter(la -> !la.getSchacHomeOrganization().equals(linkedAccount.getSchacHomeOrganization()))
                .collect(Collectors.toList());
        user.setLinkedAccounts(linkedAccounts);
        userRepository.save(user);

        logWithContext(user, "delete", "linked_account", LOG, "Deleted linked account " + linkedAccount.getSchacHomeOrganization());

        return userResponseRememberMe(user);
    }

    @PostMapping("/sp/credential")
    public ResponseEntity updatePublicKeyCredential(Authentication authentication,
                                                    @RequestBody Map<String, String> credential) {
        User user = userFromAuthentication(authentication);

        String identifier = credential.get("identifier");
        Optional<PublicKeyCredentials> publicKeyCredentials = user.getPublicKeyCredentials().stream()
                .filter(key -> key.getIdentifier().equals(identifier))
                .findFirst();
        if (!publicKeyCredentials.isPresent()) {
            return return404();
        }
        PublicKeyCredentials credentials = publicKeyCredentials.get();
        credentials.setName(credential.get("name"));
        userRepository.save(user);

        logWithContext(user, "update", "webauthn_key", LOG, "Updated publicKeyCredential " + credential.get("name"));

        return userResponseRememberMe(user);
    }

    @PutMapping("/sp/credential")
    public ResponseEntity removePublicKeyCredential(Authentication authentication,
                                                    @RequestBody Map<String, String> credential) {
        User user = userFromAuthentication(authentication);

        String identifier = credential.get("identifier");
        List<PublicKeyCredentials> publicKeyCredentials = user.getPublicKeyCredentials().stream()
                .filter(key -> !key.getIdentifier().equals(identifier))
                .collect(Collectors.toList());
        user.setPublicKeyCredentials(publicKeyCredentials);
        userRepository.save(user);

        logWithContext(user, "delete", "webauthn_key", LOG, "Deleted publicKeyCredential " + credential.get("name"));

        return userResponseRememberMe(user);
    }

    @PutMapping("/sp/service")
    public ResponseEntity<UserResponse> removeUserService(Authentication authentication,
                                                          @RequestBody DeleteServiceTokens serviceAndTokens) {
        User user = userFromAuthentication(authentication);

        String eduId = serviceAndTokens.getEduId();
        Map<String, EduID> eduIdPerServiceProvider = user.getEduIdPerServiceProvider().entrySet().stream()
                .filter(entry -> !entry.getValue().getValue().equals(eduId))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        user.setEduIdPerServiceProvider(eduIdPerServiceProvider);
        userRepository.save(user);

        logWithContext(user, "delete", "eppn", LOG, "Deleted eduID " + eduId);

        return doRemoveTokens(serviceAndTokens, user);
    }

    @PutMapping("/sp/tokens")
    public ResponseEntity<UserResponse> removeTokens(Authentication authentication,
                                                     @RequestBody DeleteServiceTokens serviceAndTokens) {
        User user = userFromAuthentication(authentication);

        return doRemoveTokens(serviceAndTokens, user);
    }

    @GetMapping("/sp/testWebAuthnUrl")
    public ResponseEntity<Map<String, String>> testWebAuthnUrl(Authentication authentication) throws UnsupportedEncodingException {
        User user = this.userFromAuthentication(authentication);
        SamlAuthenticationRequest samlAuthenticationRequest = new SamlAuthenticationRequest(true);
        samlAuthenticationRequest = authenticationRequestRepository.save(samlAuthenticationRequest);
        String email = URLEncoder.encode(user.getEmail(), Charset.defaultCharset().name());

        String loginUrl = String.format("%s/login/%s?name=Test-webAuthn&email=%s&testWebAuthn=true",
                idpBaseUrl, samlAuthenticationRequest.getId(), email);
        return ResponseEntity.status(200).body(Collections.singletonMap("url", loginUrl));
    }

    private ResponseEntity<UserResponse> doRemoveTokens(@RequestBody DeleteServiceTokens serviceAndTokens, User user) {
        List<TokenRepresentation> tokens = serviceAndTokens.getTokens();
        if (!CollectionUtils.isEmpty(tokens)) {
            openIDConnect.deleteTokens(tokens, user);
        }
        return userResponseRememberMe(user);
    }

    @GetMapping("/sp/tokens")
    public ResponseEntity<List<Map<String, Object>>> tokens(Authentication authentication) {
        User user = userFromAuthentication(authentication);
        List<Map<String, Object>> tokens = this.openIDConnect.tokens(user);
        return ResponseEntity.ok(tokens);
    }

    private User userFromAuthentication(Authentication authentication) {
        String id = ((User) authentication.getPrincipal()).getId();
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    private ResponseEntity<UserResponse> userResponseRememberMe(User user) {
        List<SamlAuthenticationRequest> samlAuthenticationRequests = authenticationRequestRepository.findByUserIdAndRememberMe(user.getId(), true);
        return ResponseEntity.ok(new UserResponse(user, convertEduIdPerServiceProvider(user), !samlAuthenticationRequests.isEmpty()));
    }


    @GetMapping("sp/security/webauthn")
    public ResponseEntity spStartWebAuthFlow(Authentication authentication) {
        //We need to go from the SP to the IdP and best is too do everything server side, but we need a temporary identifier for the user
        User user = userFromAuthentication(authentication);

        String webAuthnIdentifier = this.hash();
        user.setWebAuthnIdentifier(webAuthnIdentifier);
        userRepository.save(user);

        return ResponseEntity.status(200).body(Collections.singletonMap("token", webAuthnIdentifier));
    }

    @PostMapping("idp/security/webauthn/registration")
    public ResponseEntity idpWebAuthnRegistration(@RequestBody Map<String, String> body) throws Base64UrlException {
        String token = body.get("token");
        Optional<User> optionalUser = userRepository.findUserByWebAuthnIdentifier(token);
        if (!optionalUser.isPresent()) {
            return return404();
        }

        User user = optionalUser.get();
        if (StringUtils.isEmpty(user.getUserHandle())) {
            user.setUserHandle(this.hash());
            userRepository.save(user);
        }
        PublicKeyCredentialCreationOptions request = publicKeyCredentialCreationOptions(this.relyingParty, user);
        String challenge = request.getChallenge().getBase64Url();
        //we need to store the challenge to retrieve it later on the way back
        challengeRepository.save(new Challenge(token, challenge));
        return ResponseEntity.status(200).body(request);
    }

    /*
     * See https://github.com/Yubico/java-webauthn-server
     */
    @PutMapping("idp/security/webauthn/registration")
    public ResponseEntity idpWebAuthn(@RequestBody Map<String, Object> body) {
        try {
            return doIdpWebAuthn(body);
        } catch (Exception e) {
            //We always want to redirect back to the SP
            return ResponseEntity.status(201).body(Collections.singletonMap("location", webAuthnSpRedirectUrl));
        }
    }

    private ResponseEntity doIdpWebAuthn(Map<String, Object> body) throws IOException, Base64UrlException, RegistrationFailedException, NoSuchFieldException, IllegalAccessException {
        String token = (String) body.get("token");
        String name = (String) body.get("name");

        Optional<User> optionalUser = userRepository.findUserByWebAuthnIdentifier(token);
        if (!optionalUser.isPresent()) {
            return return404();
        }
        String credentials = (String) body.get("credentials");
        PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
                PublicKeyCredential.parseRegistrationResponseJson(credentials);

        User user = optionalUser.get();
        //remove the webAuthnIdentifier
        user.setWebAuthnIdentifier(null);

        PublicKeyCredentialCreationOptions request = this.publicKeyCredentialCreationOptions(this.relyingParty, user);
        //Needed to succeed the validation
        Challenge challenge = challengeRepository.findByToken(token).orElseThrow(ForbiddenException::new);
        this.restoreChallenge(request, ByteArray.fromBase64Url(challenge.getChallenge()));
        challengeRepository.delete(challenge);

        RegistrationResult result = this.relyingParty.finishRegistration(FinishRegistrationOptions.builder()
                .request(request)
                .response(pkc)
                .build());
        PublicKeyCredentialDescriptor keyId = result.getKeyId();
        ByteArray publicKeyCose = result.getPublicKeyCose();

        user.addPublicKeyCredential(keyId, publicKeyCose, name);
        userRepository.save(user);

        logWithContext(user, "add", "webauthn_key", LOG, "Created publicKeyCredential " + name);

        return ResponseEntity.status(201).body(Collections.singletonMap("location", webAuthnSpRedirectUrl));
    }


    @PostMapping("idp/security/webauthn/authentication")
    public ResponseEntity idpWebAuthnStartAuthentication(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        emailDomainGuard.enforceIsAllowed(email);
        emailGuessingPreventor.potentialUserEmailGuess();

        Optional<User> optionalUser = userRepository.findUserByEmailIgnoreCase(emailGuessingPreventor.sanitizeEmail(email));
        if (!optionalUser.isPresent()) {
            return return404();
        }
        User user = optionalUser.get();

        AssertionRequest request = this.relyingParty.startAssertion(StartAssertionOptions.builder()
                .username(Optional.of(user.getEmail()))
                .build());

        String authenticationRequestId = body.get("authenticationRequestId");
        String challenge = request.getPublicKeyCredentialRequestOptions().getChallenge().getBase64Url();
        // The user might have started webauthn already (and cancelled it). Need to prevent duplicates
        challengeRepository.findByToken(authenticationRequestId)
                .ifPresent(existingChallenge -> challengeRepository.delete(existingChallenge));
        challengeRepository.save(new Challenge(authenticationRequestId, challenge, user.getEmail()));

        return ResponseEntity.status(200).body(request);
    }

    @PutMapping("idp/security/webauthn/authentication")
    public ResponseEntity idpWebAuthnTryAuthentication(HttpServletRequest request, @RequestBody Map<String, Object> body) throws Base64UrlException, IOException, AssertionFailedException, NoSuchFieldException, IllegalAccessException {
        String authenticationRequestId = (String) body.get("authenticationRequestId");
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(authenticationRequestId)
                .orElseThrow(ExpiredAuthenticationException::new);

        boolean rememberMe = (boolean) body.get("rememberMe");

        PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc =
                PublicKeyCredential.parseAssertionResponseJson((String) body.get("credentials"));

        Challenge challenge = challengeRepository.findByToken(authenticationRequestId).orElseThrow(ForbiddenException::new);
        AssertionRequest assertionRequest = this.relyingParty.startAssertion(StartAssertionOptions.builder()
                .username(Optional.of(challenge.getEmail()))
                .build());
        this.restoreChallenge(assertionRequest.getPublicKeyCredentialRequestOptions(), ByteArray.fromBase64Url(challenge.getChallenge()));

        AssertionResult result = this.relyingParty.finishAssertion(FinishAssertionOptions.builder()
                .request(assertionRequest)
                .response(pkc)
                .build());

        if (!result.isSuccess()) {
            if (samlAuthenticationRequest.isTestInstance()) {
                //back to SP
                String url = String.format("%s/security?success=false", spBaseUrl);
                return ResponseEntity.status(201).body(Collections.singletonMap("url", url));
            }
            throw new ForbiddenException();
        }
        challengeRepository.delete(challenge);

        Optional<User> optionalUser = findUserStoreLanguage(challenge.getEmail());
        if (!optionalUser.isPresent()) {
            return return404();
        }
        User user = optionalUser.get();

        logLoginWithContext(user, "webauthn", true, LOG, "Successfully logged in with webauthn");

        if (samlAuthenticationRequest.isTestInstance()) {
            //back to SP
            String url = String.format("%s/security?success=true", spBaseUrl);
            return ResponseEntity.status(201).body(Collections.singletonMap("url", url));
        }

        return doMagicLink(user, samlAuthenticationRequest, rememberMe, true, request);
    }

    private PublicKeyCredentialCreationOptions publicKeyCredentialCreationOptions(RelyingParty relyingParty, User user) throws Base64UrlException {
        return relyingParty.startRegistration(StartRegistrationOptions.builder()
                .user(UserIdentity.builder()
                        .name(user.getEmail())
                        .displayName(String.format("%s %s", user.getGivenName(), user.getFamilyName()))
                        .id(ByteArray.fromBase64Url(user.getUserHandle()))
                        .build())
                .build());
    }

    private RelyingParty relyingParty(String rpId, String rpOrigin) {
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(rpId)
                .name("eduID")
                .build();
        return RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(this.userCredentialRepository)
                .origins(Collections.singleton(rpOrigin))
                .build();
    }

    protected static void restoreChallenge(Object challengeContainer, ByteArray challenge) throws NoSuchFieldException, IllegalAccessException {
        Field challengeField = challengeContainer.getClass().getDeclaredField("challenge");
        challengeField.setAccessible(true);
        challengeField.set(challengeContainer, challenge);
    }

    @GetMapping("sp/personal")
    public ResponseEntity personal(Authentication authentication) throws JsonProcessingException {
        User user = this.userFromAuthentication(authentication);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(user));
    }


    @GetMapping("/sp/logout")
    public ResponseEntity logout(HttpServletRequest request, Authentication authentication) {
        User user = this.userFromAuthentication(authentication);
        logWithContext(user, "logout", "user", LOG, "Logout");

        return doLogout(request);
    }

    @DeleteMapping("/sp/delete")
    public ResponseEntity deleteUser(Authentication authentication, HttpServletRequest request) {
        User user = userFromAuthentication(authentication);
        userRepository.delete(user);

        logWithContext(user, "delete", "account", LOG, "Delete account");

        return doLogout(request);
    }

    private ResponseEntity doLogout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Collections.singletonMap("status", "ok"));
    }

    private User verifyAndFetchUser(Authentication authentication, User deltaUser) {
        return verifyAndFetchUser(authentication, deltaUser.getId());
    }

    private User verifyAndFetchUser(Authentication authentication, String id) {
        User principal = (User) authentication.getPrincipal();
        if (!principal.getId().equals(id)) {
            throw new ForbiddenException();
        }
        //Strictly not necessary, but mid-air collisions can occur in theory
        return userRepository.findOneUserByEmailIgnoreCase(principal.getEmail());
    }

    private ResponseEntity doMagicLink(User user, SamlAuthenticationRequest samlAuthenticationRequest, boolean rememberMe,
                                       boolean passwordOrWebAuthnFlow, HttpServletRequest request) {
        samlAuthenticationRequest.setHash(hash());
        samlAuthenticationRequest.setUserId(user.getId());
        samlAuthenticationRequest.setPasswordOrWebAuthnFlow(passwordOrWebAuthnFlow);
        samlAuthenticationRequest.setRememberMe(rememberMe);
        if (rememberMe) {
            samlAuthenticationRequest.setRememberMeValue(UUID.randomUUID().toString());
        }
        authenticationRequestRepository.save(samlAuthenticationRequest);
        String serviceName = getServiceName(request, samlAuthenticationRequest);

        if (passwordOrWebAuthnFlow) {
            LOG.debug(String.format("Returning passwordOrWebAuthnFlow magic link for existing user %s", user.getUsername()));
            return ResponseEntity.status(201).body(Collections.singletonMap("url", this.magicLinkUrl + "?h=" + samlAuthenticationRequest.getHash()));
        }

        if (user.isNewUser()) {
            LOG.debug(String.format("Sending account verification mail with magic link for new user %s", user.getUsername()));
            mailBox.sendAccountVerification(user, samlAuthenticationRequest.getHash());
        } else {
            LOG.debug("Sending magic link email for existing user");
            mailBox.sendMagicLink(user, samlAuthenticationRequest.getHash(), serviceName);
        }
        return ResponseEntity.status(201).body(Collections.singletonMap("result", "ok"));
    }

    private Map<String, EduID> convertEduIdPerServiceProvider(User user) {
        return user.getEduIdPerServiceProvider();
    }

    private Optional<User> findUserStoreLanguage(String email) {
        Optional<User> optionalUser = userRepository.findUserByEmailIgnoreCase(emailGuessingPreventor.sanitizeEmail(email));
        optionalUser.ifPresent(user -> {
            String preferredLanguage = user.getPreferredLanguage();
            String language = LocaleContextHolder.getLocale().getLanguage();

            if (StringUtils.isEmpty(preferredLanguage) || !preferredLanguage.equals(language)) {
                user.setPreferredLanguage(language);
                userRepository.save(user);
            }
        });
        return optionalUser;
    }

    private ResponseEntity return404() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("status", HttpStatus.NOT_FOUND.value()));
    }

    public static String hash() {
        byte[] bytes = new byte[64];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
