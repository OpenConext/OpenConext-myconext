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
import org.springframework.http.HttpStatus;
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
    private final boolean secureCookie;

    @Autowired
    public TiqrController(@Value("${tiqr_configuration}") Resource resource,
                          EnrollmentRepository enrollmentRepository,
                          RegistrationRepository registrationRepository,
                          AuthenticationRepository authenticationRepository,
                          AuthenticationRequestRepository authenticationRequestRepository,
                          UserRepository userRepository,
                          ServiceProviderResolver serviceProviderResolver,
                          SMSService smsService,
                          @Value("${secure_cookie}") boolean secureCookie,
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
        this.tiqrService = new TiqrService(enrollmentRepository, registrationRepository, authenticationRepository, service, tiqrConfiguration.getEncryptionSecret());
        this.enrollmentRepository = enrollmentRepository;
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
        this.serviceProviderResolver = serviceProviderResolver;
        this.smsService = smsService;
        this.secureCookie = secureCookie;
        this.magicLinkUrl = magicLinkUrl;
    }

    @GetMapping("/start-enrollment")
    public ResponseEntity<Map<String, String>> startEnrollment(@RequestParam("hash") String hash) throws IOException, WriterException {
        if (!StringUtils.hasText(hash)) {
            throw new ForbiddenException("No hash parameter");
        }
        User user = getUserFromAuthenticationRequest(hash);

        Optional<Enrollment> enrollmentByUserID = enrollmentRepository.findEnrollmentByUserID(user.getId());
        enrollmentByUserID.ifPresent(enrollmentRepository::delete);

        Enrollment enrollment = tiqrService.startEnrollment(user.getId(), String.format("%s %s", user.getGivenName(), user.getFamilyName()));
        String enrollmentKey = enrollment.getKey();
        String url = String.format("%s/tiqr/metadata?enrollment_key=%s)", tiqrConfiguration.getBaseUrl(), enrollmentKey);
        Map<String, String> results = Map.of(
                "enrollmentKey", enrollmentKey,
                "url", url,
                "qrcode", QRCodeGenerator.generateQRCodeImage(url)
        );

        LOG.info(String.format("Started enrollment for %s", user.getEmail()));

        return ResponseEntity.ok(results);
    }

    private User getUserFromAuthenticationRequest(String hash) {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash)
                .orElseThrow(() -> new ForbiddenException("Unknown hash"));
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user;
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

    @GetMapping("/generate-backup-code")
    public ResponseEntity<Map<String, String>> generateBackupCode(@RequestParam("hash") String hash) {
        User user = getUserFromAuthenticationRequest(hash);
        String recoveryCode = VerificationCodeGenerator.generateBackupCode();
        user.getSurfSecureId().put(SURFSecureID.RECOVERY_CODE, recoveryCode);
        userRepository.save(user);

        tiqrService.finishRegistration(user.getId());

        Map<String, String> body = Map.of(
                "redirect", this.magicLinkUrl,
                "recoveryCode", recoveryCode);
        return getSuccessResponseEntity(body);
    }

    @PostMapping("/send-phone-code")
    public ResponseEntity<Map<String, String>> sendPhoneCode(@RequestParam("hash") String hash, @RequestBody Map<String, String> requestBody) {
        User user = getUserFromAuthenticationRequest(hash);
        String phoneVerification = VerificationCodeGenerator.generatePhoneVerification();
        String phoneNumber = requestBody.get("phoneNumber");

        smsService.send(phoneNumber, phoneVerification);

        user.getSurfSecureId().put(SURFSecureID.PHONE_VERIFICATION_CODE, phoneVerification);
        user.getSurfSecureId().put(SURFSecureID.PHONE_NUMBER, phoneNumber);
        userRepository.save(user);

        return ResponseEntity.ok(Collections.singletonMap("status", "ok"));
    }

    @PostMapping("/verify-phone-code")
    public ResponseEntity<Map<String, String>> verifyPhoneCode(@RequestParam("hash") String hash, @RequestBody Map<String, String> requestBody) {
        User user = getUserFromAuthenticationRequest(hash);
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

    private ResponseEntity<Map<String, String>> getSuccessResponseEntity(Map<String, String> body) {
        return ResponseEntity.ok(body);
    }

    @PostMapping("/start-authentication")
    public ResponseEntity<Map<String, Object>> startAuthentication(HttpServletRequest request, @Valid @RequestBody TiqrRequest tiqrRequest) throws IOException, WriterException {
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByIdAndNotExpired(tiqrRequest.getAuthenticationRequestId())
                .orElseThrow(ExpiredAuthenticationException::new);
        String email = tiqrRequest.getEmail().trim();
        User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException(String.format("User %s not found", email)));

        String requesterEntityId = samlAuthenticationRequest.getRequesterEntityId();

        logWithContext(user, "update", "user", LOG, "Updating user " + user.getEmail());
        user.computeEduIdForServiceProviderIfAbsent(requesterEntityId, serviceProviderResolver);
        userRepository.save(user);

        Optional<Cookie> optionalTiqrCookie = cookieByName(request, TIQR_COOKIE_NAME);
        boolean tiqrCookiePresent = optionalTiqrCookie.isPresent();
        Authentication authentication = tiqrService.startAuthentication(
                user.getId(),
                String.format("%s %s", user.getGivenName(), user.getFamilyName()),
                tiqrCookiePresent);
        String authenticationUrl = String.format("https://%s@%s/tiqrauth/%s/%s/%s/%s",
                user.getId(),
                this.tiqrConfiguration.getEduIdAppBaseUrl(),
                authentication.getSessionKey(),
                authentication.getChallenge(),
                this.tiqrConfiguration.getIdentifier(),
                this.tiqrConfiguration.getVersion());
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
            samlAuthenticationRequest.setHash(hash());
            samlAuthenticationRequest.setPasswordOrWebAuthnFlow(true);
            samlAuthenticationRequest.setUserId(authentication.getUserID());
            authenticationRequestRepository.save(samlAuthenticationRequest);

            body.put("redirect", this.magicLinkUrl);
            body.put("hash", samlAuthenticationRequest.getHash());
            String secure = secureCookie ? "; Secure" : "";
            String cookieValue = String.format("%s=true; Max-Age=%s; HttpOnly; %s", TIQR_COOKIE_NAME, 60 * 60 * 24 * 365, secure);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .header("Set-Cookie",cookieValue)
                    .body(body);
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

    @PutMapping("remember-me")
    public ResponseEntity<Map<String, String>> rememberMe(@RequestBody Map<String, String> body) {
        String hash = body.get("hash");
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash).orElseThrow(ExpiredAuthenticationException::new);
        samlAuthenticationRequest.setRememberMe(true);
        samlAuthenticationRequest.setRememberMeValue(UUID.randomUUID().toString());
        authenticationRequestRepository.save(samlAuthenticationRequest);
        return ResponseEntity.ok(Collections.singletonMap("status", "ok"));
    }

}
