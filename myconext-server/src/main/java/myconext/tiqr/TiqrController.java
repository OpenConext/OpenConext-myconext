package myconext.tiqr;

import com.google.zxing.WriterException;
import io.swagger.v3.oas.annotations.Hidden;
import myconext.exceptions.ExpiredAuthenticationException;
import myconext.exceptions.ForbiddenException;
import myconext.exceptions.UserNotFoundException;
import myconext.manage.ServiceProviderResolver;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import myconext.repository.*;
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

import static myconext.crypto.HashGenerator.hash;
import static myconext.log.MDCContext.logWithContext;
import static myconext.security.CookieResolver.cookieByName;
import static myconext.security.GuestIdpAuthenticationRequestFilter.TIQR_COOKIE_NAME;
import static myconext.tiqr.SURFSecureID.*;

@RestController
@RequestMapping("/tiqr")
@Hidden
public class TiqrController {

    private static final Log LOG = LogFactory.getLog(TiqrController.class);
    private static final String SESSION_KEY = "sessionKey";

    private final TiqrService tiqrService;
    private final TiqrConfiguration tiqrConfiguration;

    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;
    private final ServiceProviderResolver serviceProviderResolver;
    private final SMSService smsService;
    private final String magicLinkUrl;
    private final RegistrationRepository registrationRepository;
    private final RateLimitEnforcer rateLimitEnforcer;

    @Autowired
    public TiqrController(@Value("${tiqr_configuration}") Resource resource,
                          EnrollmentRepository enrollmentRepository,
                          RegistrationRepository registrationRepository,
                          AuthenticationRepository authenticationRepository,
                          AuthenticationRequestRepository authenticationRequestRepository,
                          UserRepository userRepository,
                          ServiceProviderResolver serviceProviderResolver,
                          SMSService smsService,
                          Environment environment,
                          @Value("${email.magic-link-url}") String magicLinkUrl) throws IOException {
        this.tiqrConfiguration = new Yaml().loadAs(resource.getInputStream(), TiqrConfiguration.class);
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

    @GetMapping("/sp/start-enrollment")
    public ResponseEntity<Map<String, String>> startEnrollment(org.springframework.security.core.Authentication authentication) throws IOException, WriterException {
        User user = userFromAuthentication(authentication);
        return doStartEnrollmentForUser(user);
    }

    @GetMapping("/sp/finish-enrollment")
    public ResponseEntity<Map<String, Object>> finishEnrollment(org.springframework.security.core.Authentication authentication) {
        User user = userFromAuthentication(authentication);
        String enrollmentVerificationKey = UUID.randomUUID().toString();
        user.setEnrollmentVerificationKey(enrollmentVerificationKey);
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("enrollmentVerificationKey", enrollmentVerificationKey));
    }

    @GetMapping("/start-enrollment")
    public ResponseEntity<Map<String, String>> startEnrollment(@RequestParam(value = "hash", required = false) String hash) throws IOException, WriterException {
        if (!StringUtils.hasText(hash)) {
            throw new ForbiddenException("No hash parameter");
        }
        User user = getUserFromAuthenticationRequest(hash);

        return doStartEnrollmentForUser(user);
    }

    private ResponseEntity<Map<String, String>> doStartEnrollmentForUser(User user) throws WriterException, IOException {
        Enrollment enrollment = tiqrService.startEnrollment(user.getId(), String.format("%s %s", user.getGivenName(), user.getFamilyName()));
        String enrollmentKey = enrollment.getKey();
        String metaDataUrl = String.format("%s/tiqr/metadata?enrollment_key=%s",
                getEduIDServerBaseUrl(),
                enrollmentKey);
        String url = String.format("%s/tiqrenroll/?metadata=%s",
                tiqrConfiguration.getEduIdAppBaseUrl(),
                encode(metaDataUrl));
        LOG.debug(String.format("Enrollment url :  %s", url));
        Map<String, String> results = Map.of(
                "enrollmentKey", enrollmentKey,
                "url", url,
                "qrcode", QRCodeGenerator.generateQRCodeImage(url)
        );

        LOG.info(String.format("Started enrollment for %s", user.getEmail()));

        return ResponseEntity.ok(results);
    }

    @GetMapping("/metadata")
    public ResponseEntity<MetaData> metaData(@RequestParam("enrollment_key") String enrollmentKey) throws TiqrException {
        MetaData metaData = tiqrService.getMetaData(enrollmentKey);

        LOG.info(String.format("Returning metaData for %s", metaData.getIdentity().getDisplayName()));

        return ResponseEntity.ok(metaData);
    }

    @GetMapping("/poll-enrollment")
    public ResponseEntity<EnrollmentStatus> enrollmentStatus(@RequestParam("enrollmentKey") String enrollmentKey) throws TiqrException {
        Enrollment enrollment = tiqrService.enrollmentStatus(enrollmentKey);

        LOG.debug(String.format("Polling enrollment for %s with status %s",
                enrollment.getUserDisplayName(), enrollment.getStatus()));

        return ResponseEntity.ok(enrollment.getStatus());
    }

    @GetMapping("/sp/generate-backup-code")
    public ResponseEntity<Map<String, String>> generateBackupCodeForSp(org.springframework.security.core.Authentication authentication) throws TiqrException {
        User user = userFromAuthentication(authentication);
        return doGenerateBackupCode(user, false);
    }

    @GetMapping("/sp/re-generate-backup-code")
    public ResponseEntity<Map<String, String>> regenerateBackupCodeForSp(HttpServletRequest request,
                                                                         org.springframework.security.core.Authentication secAuthentication) throws TiqrException {
        User user = userFromAuthentication(secAuthentication);
        String sessionKey = (String) request.getSession().getAttribute(SESSION_KEY);
        Authentication authentication = tiqrService.authenticationStatus(sessionKey);
        if (!authentication.getStatus().equals(AuthenticationStatus.SUCCESS)) {
            throw new ForbiddenException();
        }
        return doGenerateBackupCode(user, true);
    }

    @GetMapping("/generate-backup-code")
    public ResponseEntity<Map<String, String>> generateBackupCode(@RequestParam("hash") String hash) throws TiqrException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash)
                .orElseThrow(() -> new ForbiddenException("Unknown hash"));
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        samlAuthenticationRequest.setTiqrFlow(true);
        authenticationRequestRepository.save(samlAuthenticationRequest);
        return doGenerateBackupCode(user, false);
    }

    private ResponseEntity<Map<String, String>> doGenerateBackupCode(User user, boolean regenerateSpFlow) throws TiqrException {
        if (!regenerateSpFlow) {
            Registration registration = registrationRepository.findRegistrationByUserId(user.getId()).orElseThrow(IllegalArgumentException::new);
            if (!registration.getStatus().equals(RegistrationStatus.INITIALIZED)) {
                throw new ForbiddenException();
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

        Map<String, String> body = new HashMap<>();
        body.put("redirect", this.magicLinkUrl);
        body.put("recoveryCode", recoveryCode);
        return getSuccessResponseEntity(body);
    }

    @PostMapping("/sp/send-phone-code")
    public ResponseEntity<Map<String, String>> sendPhoneCodeForSp(HttpServletRequest request, org.springframework.security.core.Authentication authentication, @RequestBody Map<String, String> requestBody) {
        User user = userFromAuthentication(authentication);
        String phoneNumber = requestBody.get("phoneNumber");
        return doSendPhoneCode(user, phoneNumber, false, request);
    }

    @PostMapping("/sp/re-send-phone-code")
    public ResponseEntity<Map<String, String>> resendPhoneCodeForSp(HttpServletRequest request,
                                                                    org.springframework.security.core.Authentication secAuthentication,
                                                                    @RequestBody Map<String, String> requestBody) throws TiqrException {
        String sessionKey = (String) request.getSession().getAttribute(SESSION_KEY);
        Authentication authentication = tiqrService.authenticationStatus(sessionKey);
        if (!authentication.getStatus().equals(AuthenticationStatus.SUCCESS)) {
            throw new ForbiddenException();
        }
        User user = userFromAuthentication(secAuthentication);
        String phoneNumber = requestBody.get("phoneNumber");
        return doSendPhoneCode(user, phoneNumber, true, request);
    }

    @PostMapping("/send-phone-code")
    public ResponseEntity<Map<String, String>> sendPhoneCode(HttpServletRequest request, @RequestParam("hash") String hash, @RequestBody Map<String, String> requestBody) {
        User user = getUserFromAuthenticationRequest(hash);
        String phoneNumber = requestBody.get("phoneNumber");
        return doSendPhoneCode(user, phoneNumber, false, request);
    }

    private ResponseEntity<Map<String, String>> doSendPhoneCode(User user, String phoneNumber, boolean regenerateSpFlow, HttpServletRequest request) {
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

        return ResponseEntity.ok(Collections.singletonMap("status", "ok"));
    }

    @PostMapping("/sp/verify-phone-code")
    public ResponseEntity<Map<String, String>> spVerifyPhoneCode(org.springframework.security.core.Authentication authentication,
                                                                 @RequestBody Map<String, String> requestBody) throws TiqrException {
        User user = userFromAuthentication(authentication);
        return doVerifyPhoneCode(requestBody, user, false);
    }

    @PostMapping("/sp/re-verify-phone-code")
    public ResponseEntity<Map<String, String>> spReverifyPhoneCode(HttpServletRequest request,
                                                                   org.springframework.security.core.Authentication secAuthentication,
                                                                   @RequestBody Map<String, String> requestBody) throws TiqrException {
        User user = userFromAuthentication(secAuthentication);
        String sessionKey = (String) request.getSession().getAttribute(SESSION_KEY);
        Authentication authentication = tiqrService.authenticationStatus(sessionKey);
        if (!authentication.getStatus().equals(AuthenticationStatus.SUCCESS)) {
            throw new ForbiddenException();
        }
        return doVerifyPhoneCode(requestBody, user, true);
    }


    @PostMapping("/verify-phone-code")
    public ResponseEntity<Map<String, String>> verifyPhoneCode(@RequestParam("hash") String hash, @RequestBody Map<String, String> requestBody) throws TiqrException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash)
                .orElseThrow(() -> new ForbiddenException("Unknown hash"));
        return idpVerifyPhoneCode(requestBody, samlAuthenticationRequest);
    }

    private ResponseEntity<Map<String, String>> idpVerifyPhoneCode(@RequestBody Map<String, String> requestBody,
                                                                   SamlAuthenticationRequest samlAuthenticationRequest) throws TiqrException {
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        ResponseEntity<Map<String, String>> results = doVerifyPhoneCode(requestBody, user, false);
        //No exception
        samlAuthenticationRequest.setTiqrFlow(true);
        authenticationRequestRepository.save(samlAuthenticationRequest);
        return results;
    }

    private ResponseEntity<Map<String, String>> doVerifyPhoneCode(Map<String, String> requestBody,
                                                                  User user,
                                                                  boolean regenerateSpFlow) throws TiqrException {
        String phoneVerification = requestBody.get("phoneVerification");
        Map<String, Object> surfSecureId = user.getSurfSecureId();
        String phoneVerificationStored = (String) surfSecureId.get(PHONE_VERIFICATION_CODE);

        rateLimitEnforcer.checkRateLimit(user);

        if (MessageDigest.isEqual(phoneVerification.getBytes(StandardCharsets.UTF_8), phoneVerificationStored.getBytes(StandardCharsets.UTF_8))) {
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
            throw new ForbiddenException();
        }
        Map<String, String> results = new HashMap<>();
        results.put("redirect", this.magicLinkUrl);
        return getSuccessResponseEntity(results);
    }

    @PostMapping("/sp/start-authentication")
    public ResponseEntity<Map<String, Object>> startAuthenticationForSP(HttpServletRequest request,
                                                                        org.springframework.security.core.Authentication authentication) throws IOException, WriterException, TiqrException {
        User user = userFromAuthentication(authentication);
        ResponseEntity<Map<String, Object>> mapResponseEntity = doStartAuthentication(request, user);
        String sessionKey = (String) mapResponseEntity.getBody().get(SESSION_KEY);
        request.getSession().setAttribute(SESSION_KEY, sessionKey);
        return mapResponseEntity;
    }

    @PostMapping("/start-authentication")
    public ResponseEntity<Map<String, Object>> startAuthentication(HttpServletRequest request, @Valid @RequestBody TiqrRequest tiqrRequest) throws IOException, WriterException, TiqrException {
        authenticationRequestRepository.findByIdAndNotExpired(tiqrRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);
        String email = tiqrRequest.getEmail().trim();
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException(String.format("User %s not found", email)));

        return doStartAuthentication(request, user);
    }

    private ResponseEntity<Map<String, Object>> doStartAuthentication(HttpServletRequest request, User user) throws WriterException, IOException, TiqrException {
        Optional<Cookie> optionalTiqrCookie = cookieByName(request, TIQR_COOKIE_NAME);
        boolean tiqrCookiePresent = optionalTiqrCookie.isPresent();
        boolean sendPushNotification = tiqrCookiePresent && this.tiqrConfiguration.isPushNotificationsEnabled();
        // Reset any outstanding suspensions
        rateLimitEnforcer.unsuspendUserAfterTiqrSuccess(user);
        Authentication authentication = tiqrService.startAuthentication(
                user.getId(),
                String.format("%s %s", user.getGivenName(), user.getFamilyName()),
                this.tiqrConfiguration.getEduIdAppBaseUrl(),
                sendPushNotification);
        String authenticationUrl = authentication.getAuthenticationUrl();
        String qrCode = QRCodeGenerator.generateQRCodeImage(authenticationUrl);
        Map<String, Object> body = Map.of(
                SESSION_KEY, authentication.getSessionKey(),
                "url", authenticationUrl,
                "qr", qrCode,
                "tiqrCookiePresent", sendPushNotification && authentication.isPushNotificationSend());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/sp/poll-authentication")
    public ResponseEntity<Map<String, Object>> spAuthenticationStatus(org.springframework.security.core.Authentication authentication,
                                                                      @RequestParam(SESSION_KEY) String sessionKey) throws TiqrException {
        // Strictly speaking not necessary
        userFromAuthentication(authentication);
        return doPollAuthentication(sessionKey, Optional.empty());
    }

    @GetMapping("/poll-authentication")
    public ResponseEntity<Map<String, Object>> authenticationStatus(@RequestParam(SESSION_KEY) String sessionKey,
                                                                    @RequestParam("id") String authenticationRequestId) throws TiqrException {
        return doPollAuthentication(sessionKey, Optional.of(authenticationRequestId));
    }

    private ResponseEntity<Map<String, Object>> doPollAuthentication(String sessionKey, Optional<String> authenticationRequestIdOptional) throws TiqrException {
        Authentication authentication = tiqrService.authenticationStatus(sessionKey);
        AuthenticationStatus status = authentication.getStatus();

        LOG.debug(String.format("Polling authentication for %s with status %s",
                authentication.getUserDisplayName(), authentication.getStatus()));

        Map<String, Object> body = new HashMap<>();
        body.put("status", status.name());
        if (status.equals(AuthenticationStatus.SUCCESS)) {
            authenticationRequestIdOptional.ifPresent(authenticationRequestId -> {
                SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findById(authenticationRequestId).orElseThrow(ExpiredAuthenticationException::new);
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

                body.put("redirect", this.magicLinkUrl);
                body.put("hash", samlAuthenticationRequest.getHash());
            });
        } else if (status.equals(AuthenticationStatus.SUSPENDED)) {
            String userID = authentication.getUserID();
            User user = userRepository.findById(userID).orElseThrow(() -> new UserNotFoundException(String.format("User %s not found", authentication.getUserDisplayName())));
            Object suspendedUntil = user.getSurfSecureId().get(SURFSecureID.SUSPENDED_UNTIL);
            // Can happen, because of race condition between unsuspending and Tiqr authentication
            if (suspendedUntil != null) {
                long time = suspendedUntil instanceof Date ? ((Date) suspendedUntil).getTime() : ((Instant) suspendedUntil).getEpochSecond();
                body.put(SURFSecureID.SUSPENDED_UNTIL, time);
            } else {
                body.put(SURFSecureID.SUSPENDED_UNTIL, Instant.now().getEpochSecond());
            }
        }
        return ResponseEntity.ok(body);
    }

    @PostMapping("/sp/manual-response")
    public ResponseEntity<Map<String, String>> spManualResponse(org.springframework.security.core.Authentication authentication,
                                                                @RequestBody Map<String, String> requestBody) throws TiqrException {
        // Strictly speaking not necessary
        userFromAuthentication(authentication);
        return doManualResponse(requestBody);
    }

    @PostMapping("/manual-response")
    public ResponseEntity<Map<String, String>> manualResponse(@RequestBody Map<String, String> requestBody) throws TiqrException {
        return doManualResponse(requestBody);
    }

    private ResponseEntity<Map<String, String>> doManualResponse(Map<String, String> requestBody) throws TiqrException {
        String sessionKey = requestBody.get(SESSION_KEY);
        String response = requestBody.get("response");
        //fingers crossed, in case of mismatch an exception is thrown
        tiqrService.postAuthentication(new AuthenticationData(sessionKey, response));
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    /*
     * Endpoint called by the Tiqr app to enroll user
     */
    @PostMapping(value = "/enrollment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
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
    public ResponseEntity<Object> doAuthentication(@ModelAttribute AuthenticationData authenticationData) {
        String userId = authenticationData.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (!rateLimitEnforcer.isUserAllowedTiqrVerification(user)) {
            return ResponseEntity.ok("ERROR");
        }
        try {
            tiqrService.postAuthentication(authenticationData);

            LOG.debug("Successful authentication for user " + userId);

            rateLimitEnforcer.unsuspendUserAfterTiqrSuccess(user);
            return ResponseEntity.ok("OK");
        } catch (TiqrException | RuntimeException e) {
            //Do not show stacktrace
            LOG.error(String.format("Exception during authentication for user %s, message %s",
                    userId,
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
    public ResponseEntity<Map<String, String>> rememberMe(@RequestBody Map<String, String> body) {
        String hash = body.get("hash");
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash).orElseThrow(ExpiredAuthenticationException::new);
        samlAuthenticationRequest.setRememberMe(true);
        samlAuthenticationRequest.setRememberMeValue(UUID.randomUUID().toString());
        authenticationRequestRepository.save(samlAuthenticationRequest);
        return ResponseEntity.ok(Collections.singletonMap("status", "ok"));
    }

    @GetMapping("/sp/send-deactivation-phone-code")
    public ResponseEntity<Map<String, String>> sendDeactivationPhoneCodeForSp(HttpServletRequest request, org.springframework.security.core.Authentication authentication) {
        User user = userFromAuthentication(authentication);
        String phoneNumber = (String) user.getSurfSecureId().get(PHONE_NUMBER);
        if (!StringUtils.hasText(phoneNumber)) {
            throw new ForbiddenException();
        }
        return doSendPhoneCode(user, phoneNumber, false, request);
    }

    @PostMapping("/sp/deactivate-app")
    public ResponseEntity<Map<String, String>> deactivateApp(org.springframework.security.core.Authentication authentication,
                                                             @RequestBody Map<String, String> requestBody) {
        User user = userFromAuthentication(authentication);
        Map<String, Object> surfSecureId = user.getSurfSecureId();
        String verificationCodeKey = surfSecureId.containsKey(RECOVERY_CODE) ? RECOVERY_CODE : PHONE_VERIFICATION_CODE;
        byte[] verificationCode = ((String) surfSecureId.get(verificationCodeKey)).replaceAll(" ", "").getBytes(StandardCharsets.UTF_8);
        byte[] userVerificationCode = requestBody.get("verificationCode").replaceAll(" ", "").getBytes(StandardCharsets.UTF_8);

        rateLimitEnforcer.checkRateLimit(user);

        if (MessageDigest.isEqual(userVerificationCode, verificationCode)) {
            user.getSurfSecureId().clear();
            userRepository.save(user);
            Registration registration = registrationRepository.findRegistrationByUserId(user.getId()).orElseThrow(IllegalArgumentException::new);
            registrationRepository.delete(registration);
        } else {
            throw new ForbiddenException();
        }
        return ResponseEntity.ok(Collections.singletonMap("status", "ok"));
    }

    private User userFromAuthentication(org.springframework.security.core.Authentication authentication) {
        String userId = ((User) authentication.getPrincipal()).getId();
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private ResponseEntity<Map<String, String>> getSuccessResponseEntity(Map<String, String> body) {
        return ResponseEntity.ok(body);
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


}
