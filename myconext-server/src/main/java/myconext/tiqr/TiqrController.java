package myconext.tiqr;

import com.google.zxing.WriterException;
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
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;
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
import java.util.*;

import static myconext.crypto.HashGenerator.hash;
import static myconext.log.MDCContext.logWithContext;
import static myconext.security.CookieResolver.cookieByName;
import static myconext.security.GuestIdpAuthenticationRequestFilter.TIQR_COOKIE_NAME;

@RestController
@RequestMapping("/tiqr")
public class TiqrController {

    private static final Log LOG = LogFactory.getLog(TiqrController.class);

    private final TiqrService tiqrService;
    private final TiqrConfiguration tiqrConfiguration;

    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ServiceProviderResolver serviceProviderResolver;
    private final SMSService smsService;
    private final String magicLinkUrl;

    @Autowired
    public TiqrController(@Value("${tiqr_configuration}") Resource resource,
                          EnrollmentRepository enrollmentRepository,
                          RegistrationRepository registrationRepository,
                          AuthenticationRepository authenticationRepository,
                          AuthenticationRequestRepository authenticationRequestRepository,
                          UserRepository userRepository,
                          ServiceProviderResolver serviceProviderResolver,
                          SMSService smsService,
                          @Value("${email.magic-link-url}") String magicLinkUrl) throws IOException {
        this.tiqrConfiguration = new Yaml().loadAs(resource.getInputStream(), TiqrConfiguration.class);
        String baseUrl = tiqrConfiguration.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        Service service = new Service(
                tiqrConfiguration.getDisplayName(),
                tiqrConfiguration.getIdentifier(),
                tiqrConfiguration.getLogoUrl(),
                tiqrConfiguration.getInfoUrl(),
                String.format("%s/tiqr/authentication", baseUrl),
                this.tiqrConfiguration.isPushNotificationsEnabled(),
                String.format("%s/tiqr/enrollment", baseUrl));
        this.tiqrService = new TiqrService(enrollmentRepository,
                registrationRepository,
                authenticationRepository,
                service,
                tiqrConfiguration.getEncryptionSecret(),
                tiqrConfiguration.getApns(),
                tiqrConfiguration.getGcm());
        this.enrollmentRepository = enrollmentRepository;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.serviceProviderResolver = serviceProviderResolver;
        this.smsService = smsService;
        this.magicLinkUrl = magicLinkUrl;
    }

    @GetMapping("/sp/start-enrollment")
    public ResponseEntity<Map<String, String>> startEnrollment(org.springframework.security.core.Authentication authentication) throws IOException, WriterException {
        User user = userFromAuthentication(authentication);
        return doStartEnrollmentForUser(user);
    }

    @GetMapping("/start-enrollment")
    public ResponseEntity<Map<String, String>> startEnrollment(@RequestParam("hash") String hash) throws IOException, WriterException {
        if (!StringUtils.hasText(hash)) {
            throw new ForbiddenException("No hash parameter");
        }
        User user = getUserFromAuthenticationRequest(hash);

        return doStartEnrollmentForUser(user);
    }

    private ResponseEntity<Map<String, String>> doStartEnrollmentForUser(User user) throws WriterException, IOException {
        Optional<Enrollment> enrollmentByUserID = enrollmentRepository.findEnrollmentByUserID(user.getId());
        enrollmentByUserID.ifPresent(enrollmentRepository::delete);

        Enrollment enrollment = tiqrService.startEnrollment(user.getId(), String.format("%s %s", user.getGivenName(), user.getFamilyName()));
        String enrollmentKey = enrollment.getKey();
        String url = String.format("%s/tiqrenroll?key=%s)",
                tiqrConfiguration.getEduIdAppBaseUrl(),
                enrollmentKey);
        Map<String, String> results = Map.of(
                "enrollmentKey", enrollmentKey,
                "url", url,
                "qrcode", QRCodeGenerator.generateQRCodeImage(url)
        );

        LOG.info(String.format("Started enrollment for %s", user.getEmail()));

        return ResponseEntity.ok(results);
    }

    @GetMapping("/metadata")
    public ResponseEntity<MetaData> metaData(@RequestParam("enrollment_key") String enrollmentKey) {
        MetaData metaData = tiqrService.getMetaData(enrollmentKey);

        LOG.info(String.format("Returning metaData for %s", metaData.getIdentity().getDisplayName()));

        return ResponseEntity.ok(metaData);
    }

    @GetMapping("/poll-enrollment")
    public ResponseEntity<EnrollmentStatus> enrollmentStatus(@RequestParam("enrollmentKey") String enrollmentKey) {
        Enrollment enrollment = tiqrService.enrollmentStatus(enrollmentKey);

        LOG.debug(String.format("Polling enrollment for %s with status %s",
                enrollment.getUserDisplayName(), enrollment.getStatus()));

        return ResponseEntity.ok(enrollment.getStatus());
    }

    @GetMapping("/sp/generate-backup-code")
    public ResponseEntity<Map<String, String>> generateBackupCodeForSp(org.springframework.security.core.Authentication authentication) {
        User user = userFromAuthentication(authentication);
        return doGenerateBackupCode(user);
    }

    @GetMapping("/generate-backup-code")
    public ResponseEntity<Map<String, String>> generateBackupCode(@RequestParam("hash") String hash) {
        User user = getUserFromAuthenticationRequest(hash);
        return doGenerateBackupCode(user);
    }

    private ResponseEntity<Map<String, String>> doGenerateBackupCode(User user) {
        String recoveryCode = VerificationCodeGenerator.generateBackupCode();
        user.getSurfSecureId().put(SURFSecureID.RECOVERY_CODE, recoveryCode);
        userRepository.save(user);

        tiqrService.finishRegistration(user.getId());

        Map<String, String> body = Map.of(
                "redirect", this.magicLinkUrl,
                "recoveryCode", recoveryCode);
        return getSuccessResponseEntity(body);
    }

    @PostMapping("/sp/send-phone-code")
    public ResponseEntity<Map<String, String>> sendPhoneCodeForSp(org.springframework.security.core.Authentication authentication, @RequestBody Map<String, String> requestBody) {
        User user = userFromAuthentication(authentication);
        return doSendPhoneCode(user, requestBody);
    }

    @PostMapping("/send-phone-code")
    public ResponseEntity<Map<String, String>> sendPhoneCode(@RequestParam("hash") String hash, @RequestBody Map<String, String> requestBody) {
        User user = getUserFromAuthenticationRequest(hash);
        return doSendPhoneCode(user, requestBody);
    }

    private ResponseEntity<Map<String, String>> doSendPhoneCode(User user, Map<String, String> requestBody) {
        String phoneNumber = requestBody.get("phoneNumber");
        String phoneVerification = VerificationCodeGenerator.generatePhoneVerification();

        smsService.send(phoneNumber, phoneVerification);

        user.getSurfSecureId().put(SURFSecureID.PHONE_VERIFICATION_CODE, phoneVerification);
        user.getSurfSecureId().put(SURFSecureID.PHONE_NUMBER, phoneNumber);
        userRepository.save(user);

        return ResponseEntity.ok(Collections.singletonMap("status", "ok"));
    }

    @PostMapping("/sp/verify-phone-code")
    public ResponseEntity<Map<String, String>> doVerifyPhoneCode(org.springframework.security.core.Authentication authentication, @RequestBody Map<String, String> requestBody) {
        User user = userFromAuthentication(authentication);
        return doVerifyPhoneCode(requestBody, user);
    }

    @PostMapping("/verify-phone-code")
    public ResponseEntity<Map<String, String>> verifyPhoneCode(@RequestParam("hash") String hash, @RequestBody Map<String, String> requestBody) {
        User user = getUserFromAuthenticationRequest(hash);
        return doVerifyPhoneCode(requestBody, user);
    }

    private ResponseEntity<Map<String, String>> doVerifyPhoneCode(Map<String, String> requestBody, User user) {
        String phoneVerification = requestBody.get("phoneVerification");
        String phoneVerificationStored = (String) user.getSurfSecureId().get(SURFSecureID.PHONE_VERIFICATION_CODE);
        if (MessageDigest.isEqual(phoneVerification.getBytes(StandardCharsets.UTF_8), phoneVerificationStored.getBytes(StandardCharsets.UTF_8))) {
            user.getSurfSecureId().remove(SURFSecureID.PHONE_VERIFICATION_CODE);
            user.getSurfSecureId().put(SURFSecureID.PHONE_VERIFIED, true);
            userRepository.save(user);

            tiqrService.finishRegistration(user.getId());
        } else {
            throw new ForbiddenException();
        }
        return getSuccessResponseEntity(Collections.singletonMap("redirect", this.magicLinkUrl));
    }

    @PostMapping("/sp/start-authentication")
    public ResponseEntity<Map<String, Object>> startAuthenticationForSP(HttpServletRequest request, org.springframework.security.core.Authentication authentication) throws IOException, WriterException {
        User user = userFromAuthentication(authentication);
        return doStartAuthentication(request, user);
    }

    @PostMapping("/start-authentication")
    public ResponseEntity<Map<String, Object>> startAuthentication(HttpServletRequest request, @Valid @RequestBody TiqrRequest tiqrRequest) throws IOException, WriterException {
        authenticationRequestRepository.findByIdAndNotExpired(tiqrRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);
        String email = tiqrRequest.getEmail().trim();
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException(String.format("User %s not found", email)));

        return doStartAuthentication(request, user);
    }

    private ResponseEntity<Map<String, Object>> doStartAuthentication(HttpServletRequest request, User user) throws WriterException, IOException {
        Optional<Cookie> optionalTiqrCookie = cookieByName(request, TIQR_COOKIE_NAME);
        boolean tiqrCookiePresent = optionalTiqrCookie.isPresent();
        String authenticationUrl = String.format("https://%s/tiqrauth?u=%s&s=%s&q=%s&i=%s&v=%s",
                this.tiqrConfiguration.getEduIdAppBaseUrl(),
                encode(user.getId()),
                encode(authentication.getSessionKey()),
                encode(authentication.getChallenge()),
                encode(this.tiqrConfiguration.getIdentifier()),
                encode(this.tiqrConfiguration.getVersion()));
        Authentication authentication = tiqrService.startAuthentication(
                user.getId(),
                String.format("%s %s", user.getGivenName(), user.getFamilyName()),
                tiqrCookiePresent,
                authenticationUrl);
        String qrCode = QRCodeGenerator.generateQRCodeImage(authenticationUrl);
        Map<String, Object> body = Map.of(
                "sessionKey", authentication.getSessionKey(),
                "url", authenticationUrl,
                "qr", qrCode,
                "tiqrCookiePresent", tiqrCookiePresent);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/poll-authentication")
    public ResponseEntity<Map<String, String>> authenticationStatus(@RequestParam("sessionKey") String sessionKey,
                                                                    @RequestParam("id") String authenticationRequestId) {
        Authentication authentication = tiqrService.authenticationStatus(sessionKey);
        AuthenticationStatus status = authentication.getStatus();

        LOG.debug(String.format("Polling authentication for %s with status %s",
                authentication.getUserDisplayName(), authentication.getStatus()));

        Map<String, String> body = new HashMap<>();
        body.put("status", status.name());
        if (status.equals(AuthenticationStatus.SUCCESS)) {
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
        }
        return ResponseEntity.ok(body);
    }

    /*
     * Endpoint called by the Tiqr app to enroll user
     */
    @PostMapping(value = "/enrollment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void doEnrollment(@ModelAttribute Registration registration,
                             @RequestParam("enrollment_secret") String enrollmentSecret) {
        registration.setEnrollmentSecret(enrollmentSecret);
        //fingers crossed, in case of mismatch an exception is thrown
        tiqrService.enrollData(registration);
    }

    /*
     * Endpoint called by the Tiqr app to authenticate user
     */
    @PostMapping(value = "/authentication", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void doAuthentication(@ModelAttribute AuthenticationData authenticationData) {
        //fingers crossed, in case of mismatch an exception is thrown
        tiqrService.postAuthentication(authenticationData);
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
