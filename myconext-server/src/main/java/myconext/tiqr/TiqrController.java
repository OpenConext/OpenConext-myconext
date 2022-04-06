package myconext.tiqr;

import com.google.zxing.WriterException;
import myconext.exceptions.ForbiddenException;
import myconext.exceptions.UserNotFoundException;
import myconext.model.SamlAuthenticationRequest;
import myconext.model.User;
import myconext.repository.*;
import myconext.tiqr.TiqrConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;


import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;
import tiqr.org.TiqrService;
import tiqr.org.model.*;
import tiqr.org.secure.QRCodeGenerator;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/tiqr")
public class TiqrController {

    private final TiqrService tiqrService;
    private final TiqrConfiguration tiqrConfiguration;

    private final AuthenticationRequestRepository authenticationRequestRepository;
    private final UserRepository userRepository;

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
                String.format("%s/tiqr/enrollment", baseUrl));
        this.tiqrService = new TiqrService(enrollmentRepository, registrationRepository, authenticationRepository, service, tiqrConfiguration.getEncryptionSecret());
        this.authenticationRequestRepository = authenticationRequestRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/start")
    public ResponseEntity<Map<String, String>> startEnrollment(@RequestParam("hash") String hash) throws IOException, WriterException {
        if (!StringUtils.hasText(hash)) {
            throw new ForbiddenException("No hash parameter");
        }
        SamlAuthenticationRequest samlAuthenticationRequest = authenticationRequestRepository.findByHash(hash)
                .orElseThrow(() -> new ForbiddenException("Unknown hash"));
        String userId = samlAuthenticationRequest.getUserId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Enrollment enrollment = tiqrService.startEnrollment(user.getId(), String.format("%s %s", user.getGivenName(), user.getFamilyName()));
        String enrollmentKey = enrollment.getKey();
        String url = String.format("%s/tiqr/metadata?enrollment_key=%s)", tiqrConfiguration.getBaseUrl(), enrollmentKey);
        Map<String, String> results = Map.of(
                "enrollmentKey", enrollmentKey,
                "url", url,
                "qrcode", QRCodeGenerator.generateQRCodeImage(url)
        );
        return ResponseEntity.ok(results);
    }

    @PostMapping("/qrcode")
    public ResponseEntity<Map<String, String>> qrcode(@RequestBody Map<String, String> body) throws IOException, WriterException {
        Map<String, String> result = Collections.singletonMap("qrcode", QRCodeGenerator.generateQRCodeImage(body.get("url")));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/metadata")
    public ResponseEntity<MetaData> metaData(@RequestParam("enrollment_key") String enrollmentKey) {
        MetaData metaData = tiqrService.getMetaData(enrollmentKey);
        return ResponseEntity.ok(metaData);
    }

    @GetMapping("/poll-enrollment")
    public ResponseEntity<EnrollmentStatus> enrollmentStatus(@RequestParam("enrollmentKey") String enrollmentKey) {
        return ResponseEntity.ok(tiqrService.enrollmentStatus(enrollmentKey));
    }

    @GetMapping("/poll-authentication")
    public ResponseEntity<AuthenticationStatus> authenticationStatus(@RequestParam("sessionKey") String sessionKey) {
        return ResponseEntity.ok(tiqrService.authenticationStatus(sessionKey));
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {
        String[] denylist = new String[]{"class.*", "Class.*", "*.class.*", "*.Class.*"};
        dataBinder.setDisallowedFields(denylist);
    }

    /*
     * Endpoint called by the Tiqr app
     */
    @PostMapping(value = "/enrollment", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void brokerRequest(@ModelAttribute Registration registration,
                              @RequestParam("enrollment_secret") String enrollmentSecret) {
        registration.setEnrollmentSecret(enrollmentSecret);
        //fingers crossed, in case of mismatch an exception is thrown
        tiqrService.enrollData(registration);
    }


}
