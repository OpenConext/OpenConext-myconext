package myconext.tiqr;

import com.google.zxing.WriterException;
import myconext.exceptions.ForbiddenException;
import myconext.exceptions.UserNotFoundException;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import myconext.repository.*;
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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/tiqr")
public class TiqrController {

    private static final Log LOG = LogFactory.getLog(TiqrController.class);

    private final TiqrService tiqrService;
    private final TiqrConfiguration tiqrConfiguration;

    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Autowired
    public TiqrController(@Value("${tiqr_configuration}") Resource resource,
                          EnrollmentRepository enrollmentRepository,
                          RegistrationRepository registrationRepository,
                          AuthenticationRepository authenticationRepository,
                          AuthenticationRequestRepository authenticationRequestRepository,
                          UserRepository userRepository) throws IOException {
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
    }

    @GetMapping("/start-enrollment")
    public ResponseEntity<Map<String, String>> startEnrollment(@RequestParam("hash") String hash) throws IOException, WriterException {
        if (!StringUtils.hasText(hash)) {
            throw new ForbiddenException("No hash parameter");
        }
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash)
                .orElseThrow(() -> new ForbiddenException("Unknown hash"));
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Optional<Enrollment> enrollmentByUserID = enrollmentRepository.findEnrollmentByUserID(userId);
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

    @PostMapping("/start-authentication")
    public ResponseEntity<Map<String, String>> startAuthentication(HttpServletRequest request, @Valid @RequestBody TiqrRequest tiqrRequest) throws IOException, WriterException {

//        Optional<User> optionalUser = userRepository.findUserByEmail(emailGuessingPreventor.sanitizeEmail(email));
//
//        Optional<Cookie> optionalTiqrCookie = cookieByName(request, TIQR_COOKIE_NAME);
//        tiqrService.startAuthentication()
        return null;
    }

    @GetMapping("/poll-authentication")
    public ResponseEntity<Map<String, String>> authenticationStatus(@RequestParam("sessionKey") String sessionKey) {
        Authentication authentication = tiqrService.authenticationStatus(sessionKey);
        AuthenticationStatus status = authentication.getStatus();

        LOG.debug(String.format("Polling authentication for %s with status %s",
                authentication.getUserDisplayName(), authentication.getStatus()));

        Map<String, String> result = new HashMap<>();
        result.put("status", status.name());
        if (status.equals(AuthenticationStatus.SUCCESS)) {
            //TODO Add the URL to redirect to, picked up by GuestIdpAuthenticationRequestFilter
            result.put("url", "TODO");
        }
        return ResponseEntity.ok(result);
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

}
