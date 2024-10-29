package myconext.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.data.exception.Base64UrlException;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.SneakyThrows;
import myconext.cron.DisposableEmailProviders;
import myconext.exceptions.*;
import myconext.mail.MailBox;
import myconext.manage.Manage;
import myconext.model.*;
import myconext.oidcng.OpenIDConnect;
import myconext.repository.*;
import myconext.security.*;
import myconext.webauthn.UserCredentialRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
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
import tiqr.org.model.Registration;

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

import static myconext.SwaggerOpenIdConfig.OPEN_ID_SCHEME_NAME;
import static myconext.crypto.HashGenerator.hash;
import static myconext.log.MDCContext.logLoginWithContext;
import static myconext.log.MDCContext.logWithContext;

@RestController
@RequestMapping(value = {"/myconext/api", "/mobile/api"})
@SecurityRequirement(name = OPEN_ID_SCHEME_NAME, scopes = {"eduid.nl/mobile"})
@EnableConfigurationProperties({ServicesConfiguration.class})
public class UserController implements UserAuthentication {

    private static final Log LOG = LogFactory.getLog(UserController.class);

    private final UserRepository userRepository;
    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final MailBox mailBox;
    private final Manage manage;
    private final OpenIDConnect openIDConnect;
    private final String magicLinkUrl;
    private final String schacHomeOrganization;
    private final String webAuthnSpRedirectUrl;
    private final String idpBaseUrl;
    private final RelyingParty relyingParty;
    private final UserCredentialRepository userCredentialRepository;
    private final ChallengeRepository challengeRepository;
    private final PasswordResetHashRepository passwordResetHashRepository;
    private final ChangeEmailHashRepository changeEmailHashRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4, new SecureRandom());
    private final EmailGuessingPrevention emailGuessingPreventor;
    private final EmailDomainGuard emailDomainGuard;
    private final DisposableEmailProviders disposableEmailProviders;
    private final String spBaseUrl;
    private final ObjectMapper objectMapper;
    private final RegistrationRepository registrationRepository;
    private final boolean featureDefaultRememberMe;
    private final ServicesConfiguration servicesConfiguration;

    private final List<VerifyIssuer> issuers;
    //For now, hardcode the not known issuers from test
    private final List<String> unknownIssuers = List.of("CURRNL2A");

    public UserController(UserRepository userRepository,
                          UserCredentialRepository userCredentialRepository,
                          ChallengeRepository challengeRepository,
                          PasswordResetHashRepository passwordResetHashRepository,
                          ChangeEmailHashRepository changeEmailHashRepository,
                          AuthenticationRequestRepository authenticationRequestRepository,
                          MailBox mailBox,
                          Manage manage,
                          OpenIDConnect openIDConnect,
                          EmailDomainGuard emailDomainGuard,
                          DisposableEmailProviders disposableEmailProviders,
                          RegistrationRepository registrationRepository,
                          @Qualifier("jsonMapper") ObjectMapper objectMapper,
                          @Value("${email.magic-link-url}") String magicLinkUrl,
                          @Value("${schac_home_organization}") String schacHomeOrganization,
                          @Value("${email_guessing_sleep_millis}") int emailGuessingSleepMillis,
                          @Value("${sp_redirect_url}") String spBaseUrl,
                          @Value("${idp_redirect_url}") String idpBaseUrl,
                          @Value("${rp_origin}") String rpOrigin,
                          @Value("${rp_id}") String rpId,
                          @Value("${feature.default_remember_me}") boolean featureDefaultRememberMe,
                          @Value("${verify.issuers_path}") Resource issuersResource,
                          ServicesConfiguration servicesConfiguration) throws IOException {
        this.userRepository = userRepository;
        this.userCredentialRepository = userCredentialRepository;
        this.challengeRepository = challengeRepository;
        this.passwordResetHashRepository = passwordResetHashRepository;
        this.changeEmailHashRepository = changeEmailHashRepository;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.mailBox = mailBox;
        this.manage = manage;
        this.openIDConnect = openIDConnect;
        this.emailDomainGuard = emailDomainGuard;
        this.disposableEmailProviders = disposableEmailProviders;
        this.registrationRepository = registrationRepository;
        this.objectMapper = objectMapper;
        this.magicLinkUrl = magicLinkUrl;
        this.schacHomeOrganization = schacHomeOrganization;
        this.idpBaseUrl = idpBaseUrl;
        this.spBaseUrl = spBaseUrl;
        this.webAuthnSpRedirectUrl = String.format("%s/security", spBaseUrl);
        this.servicesConfiguration = servicesConfiguration;
        this.relyingParty = relyingParty(rpId, rpOrigin);
        this.emailGuessingPreventor = new EmailGuessingPrevention(emailGuessingSleepMillis);
        this.featureDefaultRememberMe = featureDefaultRememberMe;

        List<IdinIssuers> idinIssuers = objectMapper.readValue(issuersResource.getInputStream(), new TypeReference<>() {
        });
        //For now, we only support "Nederland"
        this.issuers = idinIssuers.get(0).getIssuers().stream().filter(issuer -> !unknownIssuers.contains(issuer.getId())).collect(Collectors.toList());

    }

    @Operation(summary = "All institutional domains",
            description = "All institutional domains which will generate a warning if a user enters an email at this domain")
    @GetMapping({"/idp/email/domain/institutional", "/sp/create-from-institution/domain/institutional"})
    public Set<String> institutionalDomains() {
        return this.manage.getDomainNames();
    }

    @Hidden
    @GetMapping({"/idp/email/domain/allowed", "/sp/create-from-institution/domain/allowed"})
    public Set<String> allowedDomains() {
        return this.emailDomainGuard.getAllowedDomains();
    }

    @Hidden
    @GetMapping("/idp/service/name/{id}")
    public Map<String, String> serviceName(@PathVariable("id") String id) {
        if ("42".equals(id)) {
            return Collections.singletonMap("name", "This Beautiful Service");
        }
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(id)
                .orElseThrow(() -> new ExpiredAuthenticationException("Expired samlAuthenticationRequest: " + id));
        return Collections.singletonMap("name",
                samlAuthenticationRequest.getServiceName());
    }

    @Hidden
    @GetMapping("/idp/me/{hash}")
    public UserResponse userInfo(@PathVariable("hash") String hash) {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash)
                .orElseThrow(() -> new ExpiredAuthenticationException("Expired authentication request"));
        User user = userRepository.findById(samlAuthenticationRequest.getUserId())
                .orElseThrow(() -> new ExpiredAuthenticationException("Expired authentication request"));
        return new UserResponse(user, null, Optional.empty(), false, manage, issuers);
    }

    @Hidden
    @PostMapping("/idp/service/email")
    public List<String> knownAccount(@RequestBody Map<String, String> email) {
        emailGuessingPreventor.potentialUserEmailGuess();
        User user = userRepository.findUserByEmail(email.get("email"))
                .orElseThrow(() -> new UserNotFoundException(String.format("User with email %s not found", email.get("email"))));
        return user.loginOptions();
    }

    @Hidden
    @GetMapping("/idp/service/hash/{hash}")
    public Map<String, String> serviceNameByHash(@PathVariable("hash") String hash) {
        return Collections.singletonMap("name",
                authenticationRequestRepository.findByHash(hash)
                        .orElseThrow(() -> new ExpiredAuthenticationException("Expired authentication request")).getServiceName());
    }

    @Hidden
    @GetMapping("/idp/service/id/{id}")
    public Map<String, String> serviceNameById(@PathVariable("id") String id) {
        return Collections.singletonMap("name",
                authenticationRequestRepository.findById(id)
                        .orElseThrow(() -> new ExpiredAuthenticationException("Expired authentication request")).getServiceName());
    }

    @Hidden
    @PostMapping("/idp/magic_link_request")
    public ResponseEntity newMagicLinkRequest(HttpServletRequest request, @Valid @RequestBody MagicLinkRequest magicLinkRequest) {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(magicLinkRequest.getAuthenticationRequestId())
                .orElseThrow(() -> new ExpiredAuthenticationException("Expired authentication request"));

        User user = magicLinkRequest.getUser();

        String email = user.getEmail();
        verifyEmails(email);

        Optional<User> optionalUser = userRepository.findUserByEmail(emailGuessingPreventor.sanitizeEmail(email));
        if (optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Collections.singletonMap("status", HttpStatus.CONFLICT.value()));
        }
        String preferredLanguage = LocaleContextHolder.getLocale().getLanguage();
        //prevent not-wanted attributes in the database
        String requesterEntityId = samlAuthenticationRequest.getRequesterEntityId();
        String schacHomeOrganization = this.emailDomainGuard.schacHomeOrganizationByDomain(this.schacHomeOrganization, email);

        User userToSave = new User(UUID.randomUUID().toString(), email, user.getGivenName(), user.getGivenName(),
                user.getFamilyName(), schacHomeOrganization, preferredLanguage, requesterEntityId, manage);
        userToSave = userRepository.save(userToSave);

        return this.doMagicLink(userToSave, samlAuthenticationRequest, false, request);
    }

    @Hidden
    @PutMapping("/idp/magic_link_request")
    public ResponseEntity magicLinkRequest(HttpServletRequest request, @Valid @RequestBody MagicLinkRequest magicLinkRequest) {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(magicLinkRequest.getAuthenticationRequestId())
                .orElseThrow(() -> new ExpiredAuthenticationException("Expired samlAuthenticationRequest: " + magicLinkRequest.getAuthenticationRequestId()));

        User providedUser = magicLinkRequest.getUser();

        String email = providedUser.getEmail();
        this.verifyEmails(email);

        Optional<User> optionalUser = findUserStoreLanguage(email);
        if (!optionalUser.isPresent()) {
            return return404();
        }
        User user = optionalUser.get();
        String requesterEntityId = samlAuthenticationRequest.getRequesterEntityId();

        logWithContext(user, "update", "user", LOG, "Updating user " + user.getEmail());
        user.computeEduIdForServiceProviderIfAbsent(requesterEntityId, manage);
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
        return doMagicLink(user, samlAuthenticationRequest, magicLinkRequest.isUsePassword(), request);
    }

    @Hidden
    @GetMapping("/idp/resend_magic_link_request")
    public ResponseEntity resendMagicLinkRequest(HttpServletRequest request, @RequestParam("id") String authenticationRequestId) {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(authenticationRequestId)
                .orElseThrow(() -> new ExpiredAuthenticationException("Expired samlAuthenticationRequest: " + authenticationRequestId));
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (user.isNewUser()) {
            sendAccountVerificationMail(samlAuthenticationRequest, user);
        } else {
            String serviceName = this.manage.getServiceName(request, samlAuthenticationRequest);
            mailBox.sendMagicLink(user, samlAuthenticationRequest.getHash(), serviceName);
        }
        return ResponseEntity.ok(true);
    }

    private void sendAccountVerificationMail(SamlAuthenticationRequest samlAuthenticationRequest, User user) {
        LOG.debug(String.format("Sending account verification mail with magic link for new user %s", user.getUsername()));
        mailBox.sendAccountVerification(user, samlAuthenticationRequest.getHash());
    }

    @Hidden
    @GetMapping("/idp/security/success")
    public int successfullyLoggedIn(@RequestParam("id") String id) {
        Optional<SamlAuthenticationRequest> optionalSamlAuthenticationRequest = authenticationRequestRepository.findById(id);
        return optionalSamlAuthenticationRequest.map(samlAuthenticationRequest -> samlAuthenticationRequest.getLoginStatus().ordinal()).orElse(LoginStatus.NOT_LOGGED_IN.ordinal());
    }

    @Operation(summary = "Institution displaynames",
            description = "Retrieve the displayNames of the Institution by the schac_home value")
    @GetMapping("/sp/institution/names")
    public ResponseEntity<IdentityProvider> institutionNames(@RequestParam(value = "schac_home") String schacHome) {
        return ResponseEntity.ok(manage.findIdentityProviderByDomainName(schacHome)
                .orElse(new IdentityProvider(new RemoteProvider(schacHome, schacHome, schacHome, null, null), null)));
    }

    @Operation(summary = "User details", description = "Retrieve the attributes of the current user")
    @GetMapping("/sp/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        User user = userFromAuthentication(authentication);
        return userResponseRememberMe(user);
    }

    @Operation(summary = "Forget me", description = "Delete the long remember-me login for the current user")
    @DeleteMapping("/sp/forget")
    public ResponseEntity<Long> forgetMe(Authentication authentication) {
        User user = userFromAuthentication(authentication);
        Long count = authenticationRequestRepository.deleteByUserId(user.getId());
        logWithContext(user, "delete", "rememberme", LOG, "Do not remember user anymore");
        return ResponseEntity.ok(count);
    }

    @Operation(summary = "Create eduID account",
            description = "Create an eduID account and sent a verification mail to the user to confirm the ownership of the email. " +
                    "<br/>Link in the validation email is <a href=\"\">https://login.{environment}.eduid.nl/mobile/api/create-from-mobile-api?h=={{hash}}</a> which" +
                    "must NOT be captured by the eduID app." +
                    "<br/>After the account is finalized server-side the user is logged in and the server redirects to " +
                    "<a href=\"\">https://login.{environment}.eduid.nl/client/mobile/created</a>" +
                    "<br/>If the URL is not properly intercepted by the eduID app, then the browser app redirects to " +
                    "<a href=\"\">eduid://client/mobile/created?new=true</a>",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created. Mail is sent to the user",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":201}")})}),
                    @ApiResponse(responseCode = "412", description = "Forbidden email domain",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":412}")})}),
                    @ApiResponse(responseCode = "409", description = "Email is in use",
                            content = {@Content(schema = @Schema(implementation = StatusResponse.class),
                                    examples = {@ExampleObject(value = "{\"status\":409}")})})})
    @PostMapping("/idp/create")
    public ResponseEntity<StatusResponse> createEduIDAccount(@Valid @RequestBody CreateAccount createAccount) {
        String email = createAccount.getEmail();
        verifyEmails(email);

        Optional<User> optionalUser = userRepository.findUserByEmail(emailGuessingPreventor.sanitizeEmail(email));
        if (optionalUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new StatusResponse(HttpStatus.CONFLICT.value()));
        }
        CreateInstitutionEduID institution = new CreateInstitutionEduID(hash(),
                email, true);
        User user = new User(
                UUID.randomUUID().toString(),
                institution.getEmail(),
                createAccount.getGivenName(),
                createAccount.getGivenName(),
                createAccount.getFamilyName(),
                this.schacHomeOrganization,
                "en",
                createAccount.getRelyingPartClientId(),
                manage);
        user.setCreateFromInstitutionKey(institution.getHash());
        user.validate();

        userRepository.save(user);

        String linkUrl = String.format("%s/mobile/api/create-from-mobile-api", this.idpBaseUrl);
        mailBox.sendAccountVerificationMobileAPI(user, institution.getHash(), linkUrl);

        logWithContext(user, "create", "user", LOG, "Create user in mobile API");

        return ResponseEntity.status(HttpStatus.CREATED).body(new StatusResponse(HttpStatus.CREATED.value()));
    }

    @Operation(summary = "Change names", description = "Update the givenName, chosenName and / or the familyName of the User")
    @PutMapping("/sp/update")
    public ResponseEntity<UserResponse> updateUserProfile(Authentication authentication, @Valid @RequestBody UpdateUserNameRequest deltaUser) {
        User user = userFromAuthentication(authentication);
        if (StringUtils.hasText(deltaUser.getGivenName())) {
            user.setChosenName(deltaUser.getGivenName());
        }
        //New API allows for update chosen name, override previous update
        if (StringUtils.hasText(deltaUser.getChosenName())) {
            user.setChosenName(deltaUser.getChosenName());
        }
        //Only if there is not validated name, we allow for updates
        if (CollectionUtils.isEmpty(user.getLinkedAccounts())) {
            if (StringUtils.hasText(deltaUser.getGivenName())) {
                user.setGivenName(deltaUser.getGivenName());
            }
            if (StringUtils.hasText(deltaUser.getFamilyName())) {
                user.setFamilyName(deltaUser.getFamilyName());
            }
        }
        user.validate();

        userRepository.save(user);
        logWithContext(user, "update", "name", LOG, "Update user profile");

        authenticationRequestRepository.deleteByUserId(user.getId());

        return returnUserResponse(user);
    }

    @Operation(summary = "Mark linkedAccount as preferred", description = "Mark linkedAccount as preferred")
    @PutMapping("/sp/prefer-linked-account")
    public ResponseEntity<UserResponse> updateLinkedAccount(Authentication authentication,
                                                            @Valid @RequestBody UpdateLinkedAccountRequest updateLinkedAccountRequest) {
        User user = userFromAuthentication(authentication);
        if (updateLinkedAccountRequest.isExternal()) {
            user.getExternalLinkedAccounts().stream()
                    .filter(externalLinkedAccount -> externalLinkedAccount.getIdpScoping().name().equals(updateLinkedAccountRequest.getIdpScoping()))
                    .findFirst()
                    .ifPresent(externalLinkedAccount -> {
                        if (StringUtils.hasText(externalLinkedAccount.getFirstName()) && IdpScoping.eherkenning.equals(externalLinkedAccount.getIdpScoping())) {
                            user.setGivenName(externalLinkedAccount.getFirstName());
                        }
                        if (StringUtils.hasText(externalLinkedAccount.getInitials()) && IdpScoping.idin.equals(externalLinkedAccount.getIdpScoping())) {
                            user.setGivenName(externalLinkedAccount.getInitials());
                        }
                        if (StringUtils.hasText(externalLinkedAccount.getLegalLastName())) {
                            user.setFamilyName(externalLinkedAccount.getLegalLastName());
                        }
                        externalLinkedAccount.setPreferred(true);
                        user.getLinkedAccounts().forEach(linkedAccount -> linkedAccount.setPreferred(false));
                    });
        } else {
            user.getLinkedAccounts().stream()
                    .filter(linkedAccount -> linkedAccount.isMatch(updateLinkedAccountRequest))
                    .findFirst()
                    .ifPresent(linkedAccount -> {
                        if (linkedAccount.areNamesValidated()) {
                            user.getLinkedAccounts().forEach(otherLinkedAccount -> otherLinkedAccount.setPreferred(false));
                            linkedAccount.setPreferred(true);
                            user.setGivenName(linkedAccount.getGivenName());
                            user.setFamilyName(linkedAccount.getFamilyName());
                            user.getExternalLinkedAccounts().forEach(account -> account.setPreferred(false));
                        }
                    });
        }
        userRepository.save(user);

        logWithContext(user, "update", "name", LOG, "Update user linked account");

        return returnUserResponse(user);
    }

    @Operation(summary = "Change email", description =
            "Request to change the email of the user. The link in the validation email is " +
                    "<a href=\"\">https://login.{environment}.eduid.nl/client/mobile/update-email?h=={{hash}}</a>" +
                    "with an unique 'h' query param which must be used in 'mobile/api/sp/confirm-email' to confirm the update." +
                    "<br/>If the URL is not properly intercepted by the eduID app, then the browser app redirects to " +
                    "<a href=\"\">eduid://client/mobile/confirm-email?h={{hash}}</a>")
    @PutMapping("/sp/email")
    public ResponseEntity<UserResponse> updateEmail(Authentication authentication, @Valid @RequestBody UpdateEmailRequest updateEmailRequest,
                                                    @RequestParam(value = "force", required = false, defaultValue = "false") boolean force) {
        User user = userFromAuthentication(authentication);
        List<PasswordResetHash> passwordResetHashes = passwordResetHashRepository.findByUserId(user.getId());
        if (!CollectionUtils.isEmpty(passwordResetHashes)) {
            if (force) {
                passwordResetHashRepository.deleteAll(passwordResetHashes);
            } else {
                throw new NotAcceptableException("Update email not allowed. Outstanding password reset for: " + user.getEmail());
            }
        }
        changeEmailHashRepository.deleteByUserId(user.getId());

        String hashValue = hash();
        String newEmail = updateEmailRequest.getEmail();
        Optional<User> optionalUser = userRepository.findUserByEmail(emailGuessingPreventor.sanitizeEmail(newEmail));
        if (optionalUser.isPresent()) {
            throw new DuplicateUserEmailException("There already exists a user with email " + newEmail);
        }

        changeEmailHashRepository.save(new ChangeEmailHash(user, newEmail, hashValue));
        logWithContext(user, "update", "update-email", LOG, "Send update email mail");

        mailBox.sendUpdateEmail(user, newEmail, hashValue, isMobileRequest(authentication));

        authenticationRequestRepository.deleteByUserId(user.getId());
        return returnUserResponse(user);
    }

    @Operation(summary = "Confirm email change",
            description = "Confirm the user has clicked on the link in the email sent after requesting to change the users email" +
                    "<br/>A confirmation email is sent to notify the user of the security change with a link to the " +
                    "security settings <a href=\"\">https://login.{environment}.eduid.nl/client/mobile/security</a>. " +
                    "<br/>If this URL is not properly intercepted by the eduID app, then the browser app redirects to " +
                    "<a href=\"\">eduid://client/mobile/security</a>")
    @GetMapping("/sp/confirm-email")
    public ResponseEntity<UserResponse> confirmUpdateEmail(Authentication authentication,
                                                           @Parameter(description = "The hash obtained from the query parameter 'h' in the URL sent to the user in the update-email")
                                                           @RequestParam(value = "h") String hash) {
        User user = userFromAuthentication(authentication);
        String oldEmail = user.getEmail();
        ChangeEmailHash changeEmailHash = changeEmailHashRepository.findByHashAndUserId(hash, user.getId())
                .orElseThrow(() -> new ForbiddenException("wrong_hash"));

        user.setEmail(changeEmailHash.getNewEmail());
        userRepository.save(user);
        authenticationRequestRepository.deleteByUserId(user.getId());
        mailBox.sendUpdateConfirmationEmail(user, oldEmail, user.getEmail(), isMobileRequest(authentication));
        return returnUserResponse(user);
    }

    @GetMapping("/sp/outstanding-email-links")
    @Operation(summary = "Get all outstanding change-emails-requests",
            description = "Get all outstanding change-emails-requests for the logged in user")
    public ResponseEntity<Boolean> outstandingEmailLinks(Authentication authentication) {
        User user = userFromAuthentication(authentication);
        List<ChangeEmailHash> emailHashes = changeEmailHashRepository.findByUserId(user.getId());

        return ResponseEntity.ok(!emailHashes.isEmpty());
    }

    @Operation(summary = "Validate password hash",
            description = "Check if a password change hash is valid and not expired")
    @GetMapping("/sp/password-reset-hash-valid")
    public ResponseEntity<Boolean> resetPasswordHashValid(Authentication authentication, @RequestParam("hash") String hash) {
        User user = userFromAuthentication(authentication);
        Optional<PasswordResetHash> optionalPasswordResetHash = passwordResetHashRepository.findByHashAndUserId(hash, user.getId());
        boolean expired = optionalPasswordResetHash.isEmpty() || optionalPasswordResetHash.get().isExpired();
        return ResponseEntity.ok(!expired);
    }

    @Operation(summary = "Update password",
            description = "Update or delete the user's password using the hash from the 'h' query param in the validation email. " +
                    "If 'newPassword' is null / empty than the password is removed.")
    @PutMapping("/sp/update-password")
    public ResponseEntity<UserResponse> updateUserPassword(Authentication authentication, @RequestBody UpdateUserSecurityRequest updateUserRequest) {
        User user = userFromAuthentication(authentication);

        boolean existingPassword = StringUtils.hasText(user.getPassword());
        String newPassword = updateUserRequest.getNewPassword();
        boolean deletePassword = !StringUtils.hasText(newPassword);

        PasswordResetHash passwordResetHash = passwordResetHashRepository
                .findByHashAndUserId(updateUserRequest.getHash(), user.getId())
                .orElseThrow(() -> new ForbiddenException("wrong_hash"));
        if (passwordResetHash.isExpired()) {
            throw new ForbiddenException("wrong_hash");
        }
        if (deletePassword) {
            user.deletePassword();
        } else {
            user.encryptPassword(newPassword, passwordEncoder);
        }

        user.setForgottenPassword(false);

        userRepository.save(user);
        passwordResetHashRepository.deleteByUserId(user.getId());

        String action = deletePassword ? "delete" : (existingPassword ? "update" : "add");
        logWithContext(user, action, "password", LOG, action + " password");
        authenticationRequestRepository.deleteByUserId(user.getId());

        return returnUserResponse(user);
    }

    @PutMapping("/sp/reset-password-link")
    @Operation(summary = "Reset password link", description = "Sent the user a mail with a link for the user to change his / hers password. " +
            "<br/>Link in the validation email is <a href=\"\">https://login.{environment}.eduid.nl/client/mobile/reset-password?h=={{hash}}</a> if" +
            " the user already had a password, otherwise " +
            "<a href=\"\">https://login.{environment}.eduid.nl/client/mobile/add-password?h=={{hash}}</a>" +
            "<br/>If the URL is not properly intercepted by the eduID app, then the browser app redirects to " +
            "<a href=\"\">eduid://client/mobile/reset-password?h={{hash}}</a>")
    public ResponseEntity<UserResponse> resetPasswordLink(Authentication authentication) {
        User user = userFromAuthentication(authentication);
        List<ChangeEmailHash> changeEmailHashes = changeEmailHashRepository.findByUserId(user.getId());
        changeEmailHashRepository.deleteAll(changeEmailHashes);

        user.setForgottenPassword(true);
        userRepository.save(user);

        String hashValue = hash();
        passwordResetHashRepository.save(new PasswordResetHash(user, hashValue));

        logWithContext(user, "update", "reset-password", LOG, "Send password reset mail");
        if (StringUtils.hasText(user.getPassword())) {
            mailBox.sendResetPassword(user, hashValue, isMobileRequest(authentication));
        } else {
            mailBox.sendAddPassword(user, hashValue, isMobileRequest(authentication));
        }

        authenticationRequestRepository.deleteByUserId(user.getId());
        return returnUserResponse(user);
    }


    @PutMapping("/sp/institution")
    @Operation(summary = "Remove linked account",
            description = "Remove linked account for a logged in user")
    public ResponseEntity<UserResponse> removeUserLinkedAccounts(Authentication authentication,
                                                                 @RequestBody UpdateLinkedAccountRequest updateLinkedAccountRequest) {
        User user = userFromAuthentication(authentication);
        if (updateLinkedAccountRequest.isExternal()) {
            //Only one external linked account is allowed
            user.getExternalLinkedAccounts().clear();
            user.setDateOfBirth(null);
        } else {
            user.getLinkedAccounts().removeIf(linkedAccount -> linkedAccount.isMatch(updateLinkedAccountRequest));
        }
        // Now we need to reconcile the new situation as the names of the User might not be correct anymore
        user.reconcileLinkedAccounts();

        userRepository.save(user);

        logWithContext(user, "delete", "linked_account", LOG, "Deleted linked account " + updateLinkedAccountRequest.getEduPersonPrincipalName());

        return userResponseRememberMe(user);
    }

    @PostMapping("/sp/credential")
    @Hidden
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
    @Hidden
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

    @Operation(summary = "Remove user service",
            description = "Remove user service by the eduID value")
    @PutMapping("/sp/service")
    public ResponseEntity<UserResponse> removeUserService(Authentication authentication,
                                                          @Valid @RequestBody DeleteService deleteService) {
        User user = userFromAuthentication(authentication);

        String entityId = deleteService.getServiceProviderEntityId();
        user.getEduIDS().forEach(eduID -> eduID.getServices().removeIf(service ->
                        entityId.equals(service.getEntityId()) || entityId.equals(service.getInstitutionGuid())));
        List<EduID> newEduIDs = user.getEduIDS().stream()
                .filter(eduID -> !eduID.getServices().isEmpty())
                .collect(Collectors.toList());
        user.setEduIDS(newEduIDs);
        userRepository.save(user);

        logWithContext(user, "delete", "eppn", LOG, "Deleted eduID " + entityId);

        return doRemoveTokens(deleteService.getTokens(), user);
    }

    @PutMapping("/sp/tokens")
    @Operation(summary = "Remove user tokens",
            description = "Remove user token for a service"
    )
    public ResponseEntity<UserResponse> removeTokens(Authentication authentication,
                                                     @RequestBody DeleteServiceTokens serviceAndTokens) {
        User user = userFromAuthentication(authentication);

        logWithContext(user, "delete", "tokens", LOG, "Deleted tokens " + serviceAndTokens.getTokens());

        return doRemoveTokens(serviceAndTokens.getTokens(), user);
    }

    @GetMapping("/sp/testWebAuthnUrl")
    @Hidden
    public ResponseEntity<Map<String, String>> testWebAuthnUrl(Authentication authentication) throws UnsupportedEncodingException {
        User user = this.userFromAuthentication(authentication);
        SamlAuthenticationRequest samlAuthenticationRequest = new SamlAuthenticationRequest(true);
        samlAuthenticationRequest = authenticationRequestRepository.save(samlAuthenticationRequest);
        String email = URLEncoder.encode(user.getEmail(), Charset.defaultCharset().name());

        String loginUrl = String.format("%s/webauthnTest/%s?name=Test-webAuthn&email=%s&testWebAuthn=true",
                idpBaseUrl, samlAuthenticationRequest.getId(), email);
        return ResponseEntity.status(200).body(Collections.singletonMap("url", loginUrl));
    }

    private ResponseEntity<UserResponse> doRemoveTokens(List<TokenRepresentation> tokens, User user) {
        if (!CollectionUtils.isEmpty(tokens)) {
            openIDConnect.deleteTokens(tokens, user);
        }
        return userResponseRememberMe(user);
    }

    @GetMapping("/sp/tokens")
    @Operation(summary = "Get all OpenID Connect tokens",
            description = "Get all OpenID Connect tokens for the logged in user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User tokens",
                            content = {@Content(array = @ArraySchema(schema = @Schema(implementation = Token.class)),
                                    examples =
                                            {@ExampleObject(value =
                                                    "[{\"expiresIn\":\"2023-03-08T08:59:17.458+00:00\"," +
                                                            "\"createdAt\":\"2022-12-08T08:59:17.458+00:00\"," +
                                                            "\"clientId\":\"student.mobility.rp.localhost\",\"clientName\":\"Student Mobility RP localhost\",\"audiences\":[\"student-mobility-home-institution-mock\",\"For localhost student mobility testing\",\"Resource Server for the Playground Client Test2\"]," +
                                                            "\"id\":\"6391a7651f7c1b41403f066f\"," +
                                                            "\"scopes\":[{\"name\":\"https://utrecht/api\"," +
                                                            "\"titles\":{},\"descriptions\":{\"en\":\"Retrieve personal information at Utrecht University \",\"nl\":\"Ophalen persoonsinformatie bij Utrecht Universiteit\"}}]")})})}
    )
    public ResponseEntity<List<Token>> tokens(Authentication authentication) {
        User user = userFromAuthentication(authentication);
        List<Token> tokens = this.openIDConnect.tokens(user);
        return ResponseEntity.ok(tokens);
    }

    private ResponseEntity<UserResponse> returnUserResponse(User user) {
        Optional<Registration> optionalRegistration = registrationRepository.findRegistrationByUserId(user.getId());
        UserResponse userResponse = new UserResponse(user, user.convertEduIdPerServiceProvider(this.servicesConfiguration), optionalRegistration, false, manage, issuers);
        return ResponseEntity.status(201).body(userResponse);
    }

    private ResponseEntity<UserResponse> userResponseRememberMe(User user) {
        List<SamlAuthenticationRequest> samlAuthenticationRequests = authenticationRequestRepository.findByUserIdAndRememberMe(user.getId(), true);
        Optional<Registration> optionalRegistration = registrationRepository.findRegistrationByUserId(user.getId());
        UserResponse userResponse = new UserResponse(user, user.convertEduIdPerServiceProvider(this.servicesConfiguration), optionalRegistration, !samlAuthenticationRequests.isEmpty(), manage, issuers);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("sp/security/webauthn")
    @Hidden
    public ResponseEntity spStartWebAuthFlow(Authentication authentication) {
        //We need to go from the SP to the IdP and best is to do everything server side, but we need a temporary identifier for the user
        User user = userFromAuthentication(authentication);

        String webAuthnIdentifier = hash();
        user.setWebAuthnIdentifier(webAuthnIdentifier);
        userRepository.save(user);

        return ResponseEntity.status(200).body(Collections.singletonMap("token", webAuthnIdentifier));
    }

    @PostMapping("idp/security/webauthn/registration")
    @Hidden
    public ResponseEntity idpWebAuthnRegistration(@RequestBody Map<String, String> body) throws Base64UrlException {
        String token = body.get("token");
        Optional<User> optionalUser = userRepository.findUserByWebAuthnIdentifier(token);
        if (!optionalUser.isPresent()) {
            return return404();
        }

        User user = optionalUser.get();
        if (!StringUtils.hasText(user.getUserHandle())) {
            user.setUserHandle(hash());
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
    @Hidden
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
        Challenge challenge = challengeRepository.findByToken(token)
                .orElseThrow(() -> new ForbiddenException("Invalid token: " + token));
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
    @Hidden
    public ResponseEntity idpWebAuthnStartAuthentication(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        verifyEmails(email);

        Optional<User> optionalUser = userRepository.findUserByEmail(emailGuessingPreventor.sanitizeEmail(email));
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
    @Hidden
    public ResponseEntity idpWebAuthnTryAuthentication(HttpServletRequest request, @RequestBody Map<String, Object> body) throws Base64UrlException, IOException, AssertionFailedException, NoSuchFieldException, IllegalAccessException {
        String authenticationRequestId = (String) body.get("authenticationRequestId");
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(authenticationRequestId)
                .orElseThrow(() -> new ExpiredAuthenticationException("Expired SamlAuthenticationRequest"));

        PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc =
                PublicKeyCredential.parseAssertionResponseJson((String) body.get("credentials"));

        Challenge challenge = challengeRepository.findByToken(authenticationRequestId)
                .orElseThrow(() -> new ForbiddenException("Challenge not found"));
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
                String credentialId = credentialId(body);
                String url = String.format("%s/credential?id=%s&success=false", spBaseUrl, credentialId);
                return ResponseEntity.status(201).body(Collections.singletonMap("url", url));
            }
            throw new ForbiddenException("Unsuccessfull SAML authentication ");
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
            String credentialId = credentialId(body);
            String url = String.format("%s/credential?id=%s&success=true", spBaseUrl, credentialId);
            return ResponseEntity.status(201).body(Collections.singletonMap("url", url));
        }

        return doMagicLink(user, samlAuthenticationRequest, true, request);
    }

    @SneakyThrows
    private String credentialId(Map<String, Object> body) {
        Map<String, Object> credential = objectMapper.readValue((String) body.get("credentials"), new TypeReference<>() {
        });
        return (String) credential.get("id");
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

    @GetMapping("/sp/personal")
    @Operation(summary = "Get personal data",
            description = "Get personal data for download",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User tokens",
                            content = {@Content(schema = @Schema(implementation = User.class))})}
    )
    public ResponseEntity<String> personal(Authentication authentication) throws JsonProcessingException {
        User user = this.userFromAuthentication(authentication);
        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        String userString = objectMapper.writeValueAsString(user);
        Map map = objectMapper.readValue(userString, Map.class);
        map.remove("password");
        map.remove("surfSecureId");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(objectWriter.writeValueAsString(map));
    }


    @GetMapping("/sp/logout")
    @Operation(summary = "Logout",
            description = "Logout the current logged in user")
    public ResponseEntity<StatusResponse> logout(HttpServletRequest request, Authentication authentication) {
        User user = this.userFromAuthentication(authentication);
        logWithContext(user, "logout", "user", LOG, "Logout");

        return doLogout(request);
    }

    @DeleteMapping("/sp/delete")
    @Operation(summary = "Delete",
            description = "Delete the current logged in user")
    public ResponseEntity<StatusResponse> deleteUser(Authentication authentication, HttpServletRequest request) {
        User user = userFromAuthentication(authentication);
        userRepository.delete(user);

        logWithContext(user, "delete", "account", LOG, "Delete account");

        return doLogout(request);
    }

    private ResponseEntity<StatusResponse> doLogout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        SecurityContextHolder.getContext().setAuthentication(null);
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new StatusResponse(HttpStatus.OK.value()));
    }

    private ResponseEntity doMagicLink(User user,
                                       SamlAuthenticationRequest samlAuthenticationRequest,
                                       boolean passwordOrWebAuthnFlow,
                                       HttpServletRequest request) {
        samlAuthenticationRequest.setHash(hash());
        samlAuthenticationRequest.setUserId(user.getId());
        samlAuthenticationRequest.setPasswordOrWebAuthnFlow(passwordOrWebAuthnFlow);
        if (this.featureDefaultRememberMe) {
            samlAuthenticationRequest.setRememberMe(true);
            samlAuthenticationRequest.setRememberMeValue(UUID.randomUUID().toString());
        }
        authenticationRequestRepository.save(samlAuthenticationRequest);
        String serviceName = this.manage.getServiceName(request, samlAuthenticationRequest);

        if (passwordOrWebAuthnFlow) {
            LOG.debug(String.format("Returning passwordOrWebAuthnFlow magic link for existing user %s", user.getUsername()));
            return ResponseEntity.status(201).body(Collections.singletonMap("url", this.magicLinkUrl + "?h=" + samlAuthenticationRequest.getHash()));
        }

        if (user.isNewUser()) {
            sendAccountVerificationMail(samlAuthenticationRequest, user);
        } else {
            LOG.debug("Sending magic link email for existing user");
            mailBox.sendMagicLink(user, samlAuthenticationRequest.getHash(), serviceName);
        }
        return ResponseEntity.status(201).body(Collections.singletonMap("result", "ok"));
    }

    private Optional<User> findUserStoreLanguage(String email) {
        Optional<User> optionalUser = userRepository.findUserByEmail(emailGuessingPreventor.sanitizeEmail(email));
        optionalUser.ifPresent(user -> {
            String preferredLanguage = user.getPreferredLanguage();
            String language = LocaleContextHolder.getLocale().getLanguage();

            if (!StringUtils.hasText(preferredLanguage) || !preferredLanguage.equals(language)) {
                user.setPreferredLanguage(language);
                userRepository.save(user);
            }
        });
        return optionalUser;
    }

    private void verifyEmails(String email) {
        this.emailDomainGuard.enforceIsAllowed(email);
        this.emailGuessingPreventor.potentialUserEmailGuess();
        this.disposableEmailProviders.verifyDisposableEmailProviders(email);
    }

    private ResponseEntity return404() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.singletonMap("status", HttpStatus.NOT_FOUND.value()));
    }

    public Manage getManage() {
        return manage;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
