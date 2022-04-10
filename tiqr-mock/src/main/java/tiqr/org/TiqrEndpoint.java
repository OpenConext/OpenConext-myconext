package tiqr.org;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import tiqr.org.model.*;
import tiqr.org.secure.Challenge;
import tiqr.org.secure.OCRA;
import tiqr.org.secure.SecretCipher;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Controller
public class TiqrEndpoint {

    private static final Log LOG = LogFactory.getLog(TiqrEndpoint.class);

    private final String environment;
    private final MongoTemplate mongoTemplate;
    private final String eduIDBaseUrl;
    private final RestTemplate restTemplate;
    private final SecretCipher secretCipher;

    @Autowired
    public TiqrEndpoint(MongoTemplate mongoTemplate,
                        @Value("${eduid.base_url}") String eduIDBaseUrl,
                        @Value("${environment}") String environment,
                        @Value("${encryption_secret}") String encryptionSecret) {
        this.mongoTemplate = mongoTemplate;
        this.eduIDBaseUrl = eduIDBaseUrl;
        this.environment = environment;
        this.restTemplate = new RestTemplate();
        this.secretCipher = new SecretCipher(encryptionSecret);

    }

    @GetMapping("/")
    public ModelAndView index() {
        Map<String, Object> body = Map.of(
                "environment", environment);
        return new ModelAndView("index", body);
    }

    @GetMapping("/enrollments")
    public ModelAndView enrollments() {
        List<Enrollment> enrollments = findAll(Enrollment.class, "status", EnrollmentStatus.INITIALIZED.name());
        Map<String, Object> body = Map.of(
                "enrollments", enrollments,
                "environment", environment
        );
        LOG.info(String.format("Returning %s enrollments", enrollments.size()));
        return new ModelAndView("enrollments", body);

    }

    @GetMapping("/enrollment/{key}")
    public ModelAndView enrollment(@PathVariable("key") String key) {
        String url = String.format("%s/tiqr/metadata?enrollment_key=%s", eduIDBaseUrl, key);
        Map metaData = restTemplate.getForEntity(url, Map.class).getBody();

        Enrollment enrollment = findEnrollment(key);

        Map<String, Object> body = Map.of(
                "enrollment", enrollment,
                "metadata", metaData,
                "environment", environment
        );
        LOG.info(String.format("Returning enrollment for %s", enrollment.getUserDisplayName()));
        return new ModelAndView("enrollment", body);
    }

    @GetMapping("/enrolled/{key}")
    public View enrolled(@PathVariable("key") String key) {
        Enrollment enrollment = findEnrollment(key);
        String url = String.format("%s/tiqr/enrollment?enrollment_secret=%s", eduIDBaseUrl, enrollment.getEnrollmentSecret());

        HttpHeaders headers = getHttpHeaders();

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("userid", enrollment.getUserID());
        map.add("secret", Challenge.generateSessionKey());
        map.add("language", "en");
        map.add("notificationType", "APNS");
        map.add("notificationAddress", "1234567890");
        map.add("version", "2");
        map.add("operation", "register");

        RequestEntity<Void> request = new RequestEntity(map, headers, HttpMethod.POST, URI.create(url));

        restTemplate.exchange(request, Void.class);

        LOG.info(String.format("Enrolled enrollment for %s", enrollment.getUserDisplayName()));

        return new RedirectView("/enrollments");
    }

    @GetMapping("/authentications")
    public ModelAndView authentications() {
        List<Authentication> authentications = findAll(Authentication.class, "status", AuthenticationStatus.PENDING.name());
        Map<String, Object> body = Map.of(
                "authentications", authentications,
                "environment", environment
        );
        return new ModelAndView("authentications", body);
    }

    @GetMapping("/authentication/{sessionKey}")
    public ModelAndView authentication(@PathVariable("sessionKey") String sessionKey) {
        Authentication authentication = findAuthentication(sessionKey);
        Map<String, Object> body = Map.of(
                "authentication", authentication,
                "environment", environment
        );
        LOG.info(String.format("Returning authentication for %s", authentication.getUserID()));
        return new ModelAndView("authentication", body);
    }

    @GetMapping("/authenticated/{sessionKey}")
    public View authenticated(@PathVariable("sessionKey") String sessionKey) {
        Authentication authentication = findAuthentication(sessionKey);
        String url = String.format("%s/tiqr/authentication", eduIDBaseUrl);

        HttpHeaders headers = getHttpHeaders();

        Registration registration = findRegistration(authentication.getUserID());
        String decryptedSecret = secretCipher.decrypt(registration.getSecret());
        String ocra = OCRA.generateOCRA(decryptedSecret, authentication.getChallenge(), sessionKey);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("sessionKey", sessionKey);
        map.add("userId", authentication.getUserID());
        map.add("response", ocra);
        map.add("language", "nl");
        map.add("operation", "login");
        map.add("notificationType", "APNS");
        map.add("notificationAddress", "1234567890");

        RequestEntity<Void> request = new RequestEntity(map, headers, HttpMethod.POST, URI.create(url));

        restTemplate.exchange(request, Void.class);

        return new RedirectView("/enrollments");
    }

    @GetMapping("/registrations")
    public ModelAndView registrations() {
        List<Registration> registrations = mongoTemplate.find(new Query(), Registration.class, "registrations");
        Map<String, Object> body = Map.of(
                "registrations", registrations,
                "environment", environment
        );
        return new ModelAndView("registrations", body);
    }

    @GetMapping("/registration/{userId}")
    public ModelAndView registration(@PathVariable("userId") String userId) {
        Registration registration = findRegistration(userId);
        Map<String, Object> body = Map.of(
                "registration", registration,
                "environment", environment
        );
        return new ModelAndView("registration", body);
    }

    @GetMapping("/delete-registration/{userId}")
    public View deleteRegistration(@PathVariable("userId") String userId) {
        Registration registration = findRegistration(userId);
        mongoTemplate.remove(registration);
        //Reset the surf secure settings
        Map user = mongoTemplate.findById(userId, Map.class, "users");
        Map<String, Object> surfSecureId = (Map<String, Object>) user.get("surfSecureId");
        surfSecureId.clear();
        user.put("lastSeenAppNudge", 1L);
        mongoTemplate.save(user, "users");

        return new RedirectView("/registrations");
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private Enrollment findEnrollment(String key) {
        return findOne(Enrollment.class, "key", key);
    }

    private Authentication findAuthentication(String sessionKey) {
        return findOne(Authentication.class, "sessionKey", sessionKey);
    }

    private Registration findRegistration(String userId) {
        return findOne(Registration.class, "userId", userId);
    }

    private <T> T findOne(Class<T> clazz, String attribute, String value) {
        Query query = new Query(Criteria.where(attribute).is(value));
        Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
        return mongoTemplate.findOne(query, clazz, document.collection());
    }

    private <T> List<T> findAll(Class<T> clazz, String attribute, String value) {
        Query query = new Query(Criteria.where(attribute).is(value));
        Document document = AnnotationUtils.findAnnotation(clazz, Document.class);
        return mongoTemplate.find(query, clazz, document.collection());
    }

}
