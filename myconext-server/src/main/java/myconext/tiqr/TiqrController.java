package myconext.tiqr;

import com.google.zxing.WriterException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import myconext.exceptions.ExpiredAuthenticationException;
import myconext.exceptions.ForbiddenException;
import myconext.exceptions.UserNotFoundException;
import myconext.manage.Manage;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import myconext.repository.*;
import myconext.security.CookieValueEncoder;
import myconext.security.UserAuthentication;
import myconext.security.VerificationCodeGenerator;
import myconext.sms.SMSService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;
import tiqr.org.DefaultTiqrService;
import tiqr.org.TiqrException;
import tiqr.org.TiqrService;
import tiqr.org.model.*;
import tiqr.org.secure.QRCodeGenerator;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import static myconext.crypto.HashGenerator.hash;
import static myconext.log.MDCContext.logWithContext;
import static myconext.security.CookieResolver.cookieByName;
import static myconext.security.GuestIdpAuthenticationRequestFilter.TIQR_COOKIE_NAME;
import static myconext.tiqr.SURFSecureID.*;

@RestController
@RequestMapping(value = {"/tiqr", "/mobile/tiqr"})
public class TiqrController implements UserAuthentication {

    private static final Log LOG = LogFactory.getLog(TiqrController.class);
    private static final String SESSION_KEY = "sessionKey";

    private final TiqrService tiqrService;
    private final TiqrConfiguration tiqrConfiguration;

    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;
    private final Manage serviceProviderResolver;
    private final SMSService smsService;
    private final String magicLinkUrl;
    private final RegistrationRepository registrationRepository;
    private final RateLimitEnforcer rateLimitEnforcer;
    private final CookieValueEncoder cookieValueEncoder;

    @Autowired
    public TiqrController(@Value("${tiqr_configuration}") Resource resource,
                          EnrollmentRepository enrollmentRepository,
                          RegistrationRepository registrationRepository,
                          AuthenticationRepository authenticationRepository,
                          AuthenticationRequestRepository authenticationRequestRepository,
                          UserRepository userRepository,
                          Manage serviceProviderResolver,
                          SMSService smsService,
                          Environment environment,
                          @Value("${email.magic-link-url}") String magicLinkUrl,
                          CookieValueEncoder cookieValueEncoder) throws IOException {
        this.tiqrConfiguration = new Yaml().loadAs(resource.getInputStream(), TiqrConfiguration.class);
        this.cookieValueEncoder = cookieValueEncoder;
        String baseUrl = getEduIDServerBaseUrl();
        Service service = new Service(
                tiqrConfiguration.getDisplayName(),
                tiqrConfiguration.getIdentifier(),
                tiqrConfiguration.getVersion(),
                tiqrConfiguration.getLogoUrl(),
                tiqrConfiguration.getInfoUrl(),
                String.format("%s/tiqr/authentication", baseUrl),
                this.tiqrConfiguration.isPushNotificationsEnabled(),
                String.format("%s/tiqr/enrollment", baseUrl));
        if (environment.getActiveProfiles().length > 0) {
            //Prevent FirebaseApp name tiqr already exists!
            tiqrConfiguration.getGcm().setAppName(UUID.randomUUID().toString());
        }
        this.tiqrService = new DefaultTiqrService(enrollmentRepository,
                registrationRepository,
                authenticationRepository,
                service,
                tiqrConfiguration.getEncryptionSecret(),
                tiqrConfiguration.getApns(),
                tiqrConfiguration.getGcm());
        this.registrationRepository = registrationRepository;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.serviceProviderResolver = serviceProviderResolver;
        this.smsService = smsService;
        this.magicLinkUrl = magicLinkUrl;
        this.rateLimitEnforcer = new RateLimitEnforcer(userRepository, tiqrConfiguration);
    }

    private String getEduIDServerBaseUrl() {
        String baseUrl = tiqrConfiguration.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    @Operation(summary = "Start enrollment", description = "Start Tiqr enrollment for the current user")
    @GetMapping("/sp/start-enrollment")
    public ResponseEntity<StartEnrollment> startEnrollment(org.springframework.security.core.Authentication authentication) throws IOException, WriterException {
        User user = userFromAuthentication(authentication);
        return doStartEnrollmentForUser(user);
    }

    @Operation(summary = "Finish enrollment", description = "Finish Tiqr enrollment for the current user")
    @GetMapping("/sp/finish-enrollment")
    public ResponseEntity<EnrollmentVerificationKey> finishEnrollment(org.springframework.security.core.Authentication authentication) {
        User user = userFromAuthentication(authentication);
        String enrollmentVerificationKey = UUID.randomUUID().toString();
        user.setEnrollmentVerificationKey(enrollmentVerificationKey);
        userRepository.save(user);
        return ResponseEntity.ok(new EnrollmentVerificationKey(enrollmentVerificationKey));
    }

    @GetMapping("/start-enrollment")
    @Hidden
    public ResponseEntity<StartEnrollment> startEnrollment(@RequestParam(value = "hash", required = false) String hash) throws IOException, WriterException {
        if (!StringUtils.hasText(hash)) {
            throw new ForbiddenException("No hash parameter");
        }
        User user = getUserFromAuthenticationRequest(hash);

        return doStartEnrollmentForUser(user);
    }

    private ResponseEntity<StartEnrollment> doStartEnrollmentForUser(User user) throws WriterException, IOException {
        Enrollment enrollment;
        try {
            enrollment = tiqrService.startEnrollment(user.getId(), String.format("%s %s", user.getGivenName(), user.getFamilyName()));
        } catch (TiqrException e) {
            throw new ForbiddenException("Can not start enrollment when there is an existing Registration");
        }
        String enrollmentKey = enrollment.getKey();
        String metaDataUrl = String.format("%s/tiqr/metadata?enrollment_key=%s",
                getEduIDServerBaseUrl(),
                enrollmentKey);
        String url = String.format("%s/tiqrenroll/?metadata=%s",
                tiqrConfiguration.getEduIdAppBaseUrl(),
                encode(metaDataUrl));

        LOG.debug(String.format("Enrollment url :  %s", url));

        StartEnrollment startEnrollment = new StartEnrollment(enrollmentKey, url, QRCodeGenerator.generateQRCodeImage(url));

        LOG.info(String.format("Started enrollment for %s", user.getEmail()));

        return ResponseEntity.ok(startEnrollment);
    }

    @GetMapping("/metadata")
    @Hidden
    public ResponseEntity<MetaData> metaData(@RequestParam("enrollment_key") String enrollmentKey) throws TiqrException {
        MetaData metaData = tiqrService.getMetaData(enrollmentKey);

        LOG.info(String.format("Returning metaData for %s", metaData.getIdentity().getDisplayName()));

        return ResponseEntity.ok(metaData);
    }

    @Operation(summary = "Poll enrollment", description = "Poll Tiqr enrollment status")
    @GetMapping("/poll-enrollment")
    public ResponseEntity<EnrollmentStatus> enrollmentStatus(@RequestParam("enrollmentKey") String enrollmentKey) throws TiqrException {
        Enrollment enrollment = tiqrService.enrollmentStatus(enrollmentKey);

        LOG.debug(String.format("Polling enrollment for %s with status %s",
                enrollment.getUserDisplayName(), enrollment.getStatus()));

        return ResponseEntity.ok(enrollment.getStatus());
    }

    @Operation(summary = "Generate back-up code", description = "Generate a back-up code for a finished authentication")
    @GetMapping("/sp/generate-backup-code")
    public ResponseEntity<GeneratedBackupCode> generateBackupCodeForSp(org.springframework.security.core.Authentication authentication) throws TiqrException {
        User user = userFromAuthentication(authentication);
        return doGenerateBackupCode(user, false);
    }

    @Operation(summary = "Generate new back-up code", description = "Generate a new back-up code for a finished authentication")
    @GetMapping("/sp/re-generate-backup-code")
    public ResponseEntity<GeneratedBackupCode> regenerateBackupCodeForSp(HttpServletRequest request,
                                                                         org.springframework.security.core.Authentication secAuthentication) throws TiqrException {
        User user = userFromAuthentication(secAuthentication);
        String sessionKey = (String) request.getSession().getAttribute(SESSION_KEY);
        Authentication authentication = tiqrService.authenticationStatus(sessionKey);
        if (!authentication.getStatus().equals(AuthenticationStatus.SUCCESS)) {
            throw new ForbiddenException("Forbidden backup code, wrong status: " + authentication.getStatus());
        }
        return doGenerateBackupCode(user, true);
    }

    @GetMapping("/generate-backup-code")
    @Hidden
    public ResponseEntity<GeneratedBackupCode> generateBackupCode(
            @RequestParam("hash") @Parameter(description = "Hash 'h' query parameter") String hash) throws TiqrException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash)
                .orElseThrow(() -> new ForbiddenException("Unknown hash"));
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        samlAuthenticationRequest.setTiqrFlow(true);
        authenticationRequestRepository.save(samlAuthenticationRequest);
        return doGenerateBackupCode(user, false);
    }

    private ResponseEntity<GeneratedBackupCode> doGenerateBackupCode(User user, boolean regenerateSpFlow) throws TiqrException {
        if (!regenerateSpFlow) {
            Registration registration = registrationRepository.findRegistrationByUserId(user.getId()).orElseThrow(IllegalArgumentException::new);
            if (!registration.getStatus().equals(RegistrationStatus.INITIALIZED)) {
                throw new ForbiddenException("Forbidden backup code, wrong status: " + registration.getStatus());
            }
        }
        Map<String, Object> surfSecureId = user.getSurfSecureId();
        if (regenerateSpFlow) {
            List.of(RECOVERY_CODE, PHONE_VERIFICATION_CODE, PHONE_VERIFIED, PHONE_NUMBER)
                    .forEach(surfSecureId::remove);
        }
        String recoveryCode = (String) surfSecureId
                .computeIfAbsent(RECOVERY_CODE, k -> VerificationCodeGenerator.generateBackupCode().replaceAll(" ", ""));
        userRepository.save(user);

        if (!regenerateSpFlow) {
            tiqrService.finishRegistration(user.getId());
        }
        return ResponseEntity.ok(new GeneratedBackupCode(this.magicLinkUrl, recoveryCode));
    }

    @Operation(summary = "Send phone code", description = "Send a verification code to mobile phone for a finished authentication")
    @PostMapping("/sp/send-phone-code")
    public ResponseEntity<FinishEnrollment> sendPhoneCodeForSp(HttpServletRequest request,
                                                               org.springframework.security.core.Authentication authentication,
                                                               @Valid @RequestBody PhoneCode phoneCode) {
        User user = userFromAuthentication(authentication);
        String phoneNumber = phoneCode.getPhoneNumber();
        return doSendPhoneCode(user, phoneNumber, false, request);
    }

    @Operation(summary = "Send new phone code", description = "Send a new verification code to mobile phone for a finished authentication")
    @PostMapping("/sp/re-send-phone-code")
    public ResponseEntity<FinishEnrollment> resendPhoneCodeForSp(HttpServletRequest request,
                                                                 org.springframework.security.core.Authentication secAuthentication,
                                                                 @Valid @RequestBody PhoneCode phoneCode) throws TiqrException {
        String sessionKey = (String) request.getSession().getAttribute(SESSION_KEY);
        Authentication authentication = tiqrService.authenticationStatus(sessionKey);
        if (!authentication.getStatus().equals(AuthenticationStatus.SUCCESS)) {
            throw new ForbiddenException("Forbidden phone code, wrong status: " + authentication.getStatus());
        }
        User user = userFromAuthentication(secAuthentication);
        String phoneNumber = phoneCode.getPhoneNumber();
        return doSendPhoneCode(user, phoneNumber, true, request);
    }

    private ResponseEntity<FinishEnrollment> doSendPhoneCode(User user, String phoneNumber, boolean regenerateSpFlow, HttpServletRequest request) {
        rateLimitEnforcer.checkSendSMSRateLimit(user);

        LOG.info(String.format("Sending SMS for user %s to number %s", user.getEmail(), phoneNumber));

        String phoneVerification = VerificationCodeGenerator.generatePhoneVerification();

        smsService.send(phoneNumber, phoneVerification, request.getLocale());

        Map<String, Object> surfSecureId = user.getSurfSecureId();
        surfSecureId.put(PHONE_VERIFICATION_CODE, phoneVerification);
        surfSecureId.remove(RATE_LIMIT);

        if (regenerateSpFlow) {
            surfSecureId.put(NEW_UNVERIFIED_PHONE_NUMBER, phoneNumber);
        } else {
            surfSecureId.put(PHONE_NUMBER, phoneNumber);
        }

        userRepository.save(user);

        return ResponseEntity.ok(new FinishEnrollment("ok"));
    }

    @Operation(summary = "Verify phone code", description = "Verify verification code for a finished authentication")
    @PostMapping("/sp/verify-phone-code")
    public ResponseEntity<VerifyPhoneCode> spVerifyPhoneCode(org.springframework.security.core.Authentication authentication,
                                                             @Valid @RequestBody PhoneVerification phoneVerification) throws TiqrException {
        User user = userFromAuthentication(authentication);
        return doVerifyPhoneCode(phoneVerification, user, false);
    }

    @Operation(summary = "Verify phone code again", description = "Verify verification code again for a finished authentication")
    @PostMapping("/sp/re-verify-phone-code")
    public ResponseEntity<VerifyPhoneCode> spReverifyPhoneCode(HttpServletRequest request,
                                                               org.springframework.security.core.Authentication secAuthentication,
                                                               @Valid @RequestBody PhoneVerification phoneVerification) throws TiqrException {
        User user = userFromAuthentication(secAuthentication);
        String sessionKey = (String) request.getSession().getAttribute(SESSION_KEY);
        Authentication authentication = tiqrService.authenticationStatus(sessionKey);
        if (!authentication.getStatus().equals(AuthenticationStatus.SUCCESS)) {
            throw new ForbiddenException("Forbidden phone code, wrong status: " + authentication.getStatus());
        }
        return doVerifyPhoneCode(phoneVerification, user, true);
    }


    @PostMapping("/verify-phone-code")
    @Hidden
    public ResponseEntity<VerifyPhoneCode> verifyPhoneCode(@RequestParam("hash") String hash,
                                                           @Valid @RequestBody PhoneVerification phoneVerification) throws TiqrException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash)
                .orElseThrow(() -> new ForbiddenException("Unknown hash"));
        return idpVerifyPhoneCode(phoneVerification, samlAuthenticationRequest);
    }

    private ResponseEntity<VerifyPhoneCode> idpVerifyPhoneCode(PhoneVerification phoneVerification,
                                                               SamlAuthenticationRequest samlAuthenticationRequest) throws TiqrException {
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        ResponseEntity<VerifyPhoneCode> verifyPhoneCodeResponseEntity = doVerifyPhoneCode(phoneVerification, user, false);
        //No exception
        samlAuthenticationRequest.setTiqrFlow(true);
        authenticationRequestRepository.save(samlAuthenticationRequest);
        return verifyPhoneCodeResponseEntity;
    }

    private ResponseEntity<VerifyPhoneCode> doVerifyPhoneCode(PhoneVerification phoneVerification,
                                                              User user,
                                                              boolean regenerateSpFlow) throws TiqrException {
        String code = phoneVerification.getPhoneVerification();
        Map<String, Object> surfSecureId = user.getSurfSecureId();
        String phoneVerificationStored = (String) surfSecureId.get(PHONE_VERIFICATION_CODE);

        rateLimitEnforcer.checkRateLimit(user);

        if (MessageDigest.isEqual(code.getBytes(StandardCharsets.UTF_8), phoneVerificationStored.getBytes(StandardCharsets.UTF_8))) {
            surfSecureId.remove(PHONE_VERIFICATION_CODE);
            surfSecureId.put(PHONE_VERIFIED, true);
            surfSecureId.remove(RATE_LIMIT);
            if (regenerateSpFlow) {
                String unverifiedPhoneNumber = (String) surfSecureId.get(NEW_UNVERIFIED_PHONE_NUMBER);
                surfSecureId.put(PHONE_NUMBER, unverifiedPhoneNumber);
                surfSecureId.remove(NEW_UNVERIFIED_PHONE_NUMBER);
                surfSecureId.remove(RECOVERY_CODE);
            } else {
                tiqrService.finishRegistration(user.getId());
            }
            userRepository.save(user);
        } else {
            throw new ForbiddenException("Forbidden phone code, wrong code: " + code);
        }
        return ResponseEntity.ok(new VerifyPhoneCode(this.magicLinkUrl));
    }

    @Operation(summary = "Start authentication", description = "Start Tiqr authentication for current user")
    @PostMapping("/sp/start-authentication")
    public ResponseEntity<StartAuthentication> startAuthenticationForSP(HttpServletRequest request,
                                                                        org.springframework.security.core.Authentication authentication) throws IOException, WriterException, TiqrException {
        User user = userFromAuthentication(authentication);
        ResponseEntity<StartAuthentication> startAuthenticationResponseEntity = doStartAuthentication(request, user);
        String sessionKey = startAuthenticationResponseEntity.getBody().getSessionKey();
        request.getSession().setAttribute(SESSION_KEY, sessionKey);
        return startAuthenticationResponseEntity;
    }

    @PostMapping("/start-authentication")
    @Hidden
    public ResponseEntity<StartAuthentication> startAuthentication(HttpServletRequest request,
                                                                   @Valid @RequestBody TiqrRequest tiqrRequest) throws IOException, WriterException, TiqrException {
        authenticationRequestRepository.findByIdAndNotExpired(tiqrRequest.getAuthenticationRequestId())
                .orElseThrow(() -> new ExpiredAuthenticationException("Expired tiqrRequest:" + tiqrRequest.getEmail()));
        String email = tiqrRequest.getEmail().trim();
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException(String.format("User %s not found", email)));

        return doStartAuthentication(request, user);
    }

    private ResponseEntity<StartAuthentication> doStartAuthentication(HttpServletRequest request, User user) throws WriterException, IOException, TiqrException {
        Optional<Cookie> optionalTiqrCookie = cookieByName(request, TIQR_COOKIE_NAME);
        AtomicBoolean tiqrCookieValid = new AtomicBoolean(false);
        optionalTiqrCookie.ifPresent(tiqrCookie -> tiqrCookieValid.set(this.cookieValueEncoder.matches(user.getUsername(), tiqrCookie.getValue())));
        if (optionalTiqrCookie.isPresent() && this.tiqrConfiguration.isPushNotificationsEnabled() && !tiqrCookieValid.get()) {
            LOG.info(String.format("Tiqr cookie present for user %s and push notifications are enabled, but not sending push notification. Encoded cookie value does not match username",
                    user.getEmail()));
        }
        // Reset any outstanding suspensions
        rateLimitEnforcer.unsuspendUserAfterTiqrSuccess(user);
        boolean sendPushNotification = tiqrCookieValid.get() && this.tiqrConfiguration.isPushNotificationsEnabled();
        Authentication authentication = tiqrService.startAuthentication(
                user.getId(),
                String.format("%s %s", user.getGivenName(), user.getFamilyName()),
                this.tiqrConfiguration.getEduIdAppBaseUrl(),
                sendPushNotification);
        String authenticationUrl = authentication.getAuthenticationUrl();
        String qrCode = QRCodeGenerator.generateQRCodeImage(authenticationUrl);
        StartAuthentication startAuthentication = new StartAuthentication(authentication.getSessionKey(), authenticationUrl, qrCode, sendPushNotification && authentication.isPushNotificationSend());
        return ResponseEntity.ok(startAuthentication);
    }

    @Operation(summary = "Poll authentication", description = "Poll Tiqr authentication status for current user")
    @GetMapping("/sp/poll-authentication")
    public ResponseEntity<PollAuthenticationResult> spAuthenticationStatus(org.springframework.security.core.Authentication authentication,
                                                                           @RequestParam(SESSION_KEY) @Parameter(description = "Session key of the authentication") String sessionKey) throws TiqrException {
        // Strictly speaking not necessary
        userFromAuthentication(authentication);
        return doPollAuthentication(sessionKey, Optional.empty());
    }

    @GetMapping("/poll-authentication")
    @Hidden
    public ResponseEntity<PollAuthenticationResult> authenticationStatus(@RequestParam(SESSION_KEY) String sessionKey,
                                                                         @RequestParam("id") String authenticationRequestId) throws TiqrException {
        return doPollAuthentication(sessionKey, Optional.of(authenticationRequestId));
    }

    private ResponseEntity<PollAuthenticationResult> doPollAuthentication(String sessionKey, Optional<String> authenticationRequestIdOptional) throws TiqrException {
        Authentication authentication = tiqrService.authenticationStatus(sessionKey);
        AuthenticationStatus status = authentication.getStatus();

        LOG.debug(String.format("Polling authentication for %s with status %s",
                authentication.getUserDisplayName(), authentication.getStatus()));

        PollAuthenticationResult pollAuthenticationResult = new PollAuthenticationResult();
        pollAuthenticationResult.setStatus(status.name());
        if (status.equals(AuthenticationStatus.SUCCESS)) {
            authenticationRequestIdOptional.ifPresent(authenticationRequestId -> {
                SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId)
                        .orElseThrow(() -> new ExpiredAuthenticationException("Expired samlAuthenticationRequest:" + authenticationRequestId));
                String requesterEntityId = samlAuthenticationRequest.getRequesterEntityId();

                String userID = authentication.getUserID();
                User user = userRepository.findById(userID).orElseThrow(() -> new UserNotFoundException(String.format("User %s not found", authentication.getUserDisplayName())));

                logWithContext(user, "update", "user", LOG, "Updating user " + user.getEmail());

                user.computeEduIdForServiceProviderIfAbsent(requesterEntityId, serviceProviderResolver);
                userRepository.save(user);

                samlAuthenticationRequest.setHash(hash());
                samlAuthenticationRequest.setTiqrFlow(true);
                samlAuthenticationRequest.setUserId(userID);
                authenticationRequestRepository.save(samlAuthenticationRequest);

                pollAuthenticationResult.setRedirect(this.magicLinkUrl);
                pollAuthenticationResult.setHash(samlAuthenticationRequest.getHash());
            });
        } else if (status.equals(AuthenticationStatus.SUSPENDED)) {
            String userID = authentication.getUserID();
            User user = userRepository.findById(userID).orElseThrow(() -> new UserNotFoundException(String.format("User %s not found", authentication.getUserDisplayName())));
            Object suspendedUntil = user.getSurfSecureId().get(SURFSecureID.SUSPENDED_UNTIL);
            // Can happen, because of race condition between unsuspending and Tiqr authentication
            if (suspendedUntil != null) {
                long time = suspendedUntil instanceof Date ? ((Date) suspendedUntil).getTime() : ((Instant) suspendedUntil).getEpochSecond();
                pollAuthenticationResult.setSuspendedUntil(time);
            } else {
                pollAuthenticationResult.setSuspendedUntil(Instant.now().getEpochSecond());
            }
        }
        return ResponseEntity.ok(pollAuthenticationResult);
    }

    @Operation(summary = "Manual authentication", description = "Manual Tiqr authentication response")
    @PostMapping("/sp/manual-response")
    public ResponseEntity<FinishEnrollment> spManualResponse(org.springframework.security.core.Authentication authentication,
                                                             @Valid @RequestBody ManualResponse manualResponse) throws TiqrException {
        // Strictly speaking not necessary
        userFromAuthentication(authentication);
        return doManualResponse(manualResponse);
    }

    @PostMapping("/manual-response")
    @Hidden
    public ResponseEntity<FinishEnrollment> manualResponse(@Valid @RequestBody ManualResponse manualResponse) throws TiqrException {
        return doManualResponse(manualResponse);
    }

    private ResponseEntity<FinishEnrollment> doManualResponse(ManualResponse manualResponse) throws TiqrException {
        String sessionKey = manualResponse.getSessionKey();
        String response = manualResponse.getResponse();
        //fingers crossed, in case of mismatch an exception is thrown
        tiqrService.postAuthentication(new AuthenticationData(sessionKey, response));
        return ResponseEntity.ok(new FinishEnrollment("ok"));
    }

    /*
     * Endpoint called by the Tiqr app to enroll user
     */
    @PostMapping(value = "/enrollment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Hidden
    public ResponseEntity<Object> doEnrollment(@ModelAttribute Registration registration,
                                               @RequestParam("enrollment_secret") String enrollmentSecret) {
        registration.setEnrollmentSecret(enrollmentSecret);
        try {
            Registration savedRegistration = tiqrService.enrollData(registration);
            LOG.debug("Successful enrollment for user " + savedRegistration.getUserId());
            return ResponseEntity.ok("OK");
        } catch (TiqrException | RuntimeException e) {
            LOG.error("Exception during enrollment for user: " + registration.getUserId(), e);
            return ResponseEntity.ok("ERROR");
        }
    }

    /*
     * Endpoint called by the Tiqr app to authenticate user
     */
    @PostMapping(value = "/authentication", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Hidden
    public ResponseEntity<Object> doAuthentication(@ModelAttribute AuthenticationData authenticationData) {
        String metaDataIdentity = authenticationData.getUserId();
        /*
         * This used to be the userID, but in https://github.com/OpenConext/OpenConext-myconext/issues/552 this has
         * changed to the registrationID. We need to try them both to be backwards compatible
         */
        Optional<Registration> optionalRegistration = registrationRepository.findById(metaDataIdentity);
        Optional<User> optionalUser = optionalRegistration
                .map(registration -> userRepository.findById(registration.getUserId()))
                .flatMap(Function.identity());
        User user = optionalUser
                .orElseGet(() -> userRepository.findById(metaDataIdentity)
                        .orElseThrow(() -> new UserNotFoundException("User not found with authenticationData#userId:" + metaDataIdentity)));

        if (!rateLimitEnforcer.isUserAllowedTiqrVerification(user)) {
            return ResponseEntity.ok("ERROR");
        }
        try {
            tiqrService.postAuthentication(authenticationData);

            LOG.debug(String.format("Successful authentication for user %s, %s", user.getEmail(), user.getId()));

            rateLimitEnforcer.unsuspendUserAfterTiqrSuccess(user);
            return ResponseEntity.ok("OK");
        } catch (TiqrException | RuntimeException e) {
            //Do not show stacktrace
            LOG.error(String.format("Exception during authentication for user %s, %s message %s",
                    user.getEmail(),
                    user.getId(),
                    e.getMessage()));
            rateLimitEnforcer.suspendUserAfterTiqrFailure(user);
            try {
                tiqrService.suspendAuthentication(authenticationData.getSessionKey());
            } catch (TiqrException ex) {
                //Normally bad practice, but nothing can be done about it
            }
            return ResponseEntity.ok("ERROR");
        }
    }

    @PutMapping("/remember-me")
    @Hidden
    public ResponseEntity<FinishEnrollment> rememberMe(@RequestBody Map<String, String> body) {
        String hash = body.get("hash");
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash)
                .orElseThrow(() -> new ExpiredAuthenticationException("Expired samlAuthenticationRequest: " + hash));
        samlAuthenticationRequest.setRememberMe(true);
        samlAuthenticationRequest.setRememberMeValue(UUID.randomUUID().toString());
        authenticationRequestRepository.save(samlAuthenticationRequest);
        return ResponseEntity.ok(new FinishEnrollment("ok"));
    }

    @Operation(summary = "Send de-activation code", description = "Send a de-activation code to the user")
    @GetMapping("/sp/send-deactivation-phone-code")
    public ResponseEntity<FinishEnrollment> sendDeactivationPhoneCodeForSp(HttpServletRequest request, org.springframework.security.core.Authentication authentication) {
        User user = userFromAuthentication(authentication);
        String phoneNumber = (String) user.getSurfSecureId().get(PHONE_NUMBER);
        if (!StringUtils.hasText(phoneNumber)) {
            throw new ForbiddenException("Forbidden empty phone number");
        }
        return doSendPhoneCode(user, phoneNumber, false, request);
    }

    @Operation(summary = "De-activate the app", description = "De-activate the eduID app for the current user")
    @PostMapping("/sp/deactivate-app")
    public ResponseEntity<FinishEnrollment> deactivateApp(org.springframework.security.core.Authentication authentication,
                                                          @Valid @RequestBody DeactivateRequest deactivateRequest) {
        User user = userFromAuthentication(authentication);
        Map<String, Object> surfSecureId = user.getSurfSecureId();
        String verificationCodeKey = surfSecureId.containsKey(RECOVERY_CODE) ? RECOVERY_CODE : PHONE_VERIFICATION_CODE;
        byte[] verificationCode = ((String) surfSecureId.get(verificationCodeKey)).replaceAll(" ", "").getBytes(StandardCharsets.UTF_8);
        byte[] userVerificationCode = deactivateRequest.getVerificationCode().replaceAll(" ", "").getBytes(StandardCharsets.UTF_8);

        rateLimitEnforcer.checkRateLimit(user);

        if (MessageDigest.isEqual(userVerificationCode, verificationCode)) {
            user.getSurfSecureId().clear();
            userRepository.save(user);
            Registration registration = registrationRepository.findRegistrationByUserId(user.getId()).orElseThrow(IllegalArgumentException::new);
            registrationRepository.delete(registration);
        } else {
            throw new ForbiddenException("Forbidden userVerificationCode: " + userVerificationCode);
        }
        return ResponseEntity.ok(new FinishEnrollment("ok"));
    }

    private User getUserFromAuthenticationRequest(String hash) {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash)
                .orElseThrow(() -> new ForbiddenException("Unknown hash"));
        String userId = samlAuthenticationRequest.getUserId();
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private String encode(String s) {
        return URLEncoder.encode(s, Charset.defaultCharset());
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }
}
