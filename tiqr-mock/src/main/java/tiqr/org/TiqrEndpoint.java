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
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;
import tiqr.org.model.Enrollment;
import tiqr.org.model.EnrollmentStatus;
import tiqr.org.model.MetaData;
import tiqr.org.secure.Challenge;

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

    @Autowired
    public TiqrEndpoint(MongoTemplate mongoTemplate,
                        @Value("${eduid.base_url}") String eduIDBaseUrl,
                        @Value("${environment}") String environment) {
        this.mongoTemplate = mongoTemplate;
        this.eduIDBaseUrl = eduIDBaseUrl;
        this.environment = environment;
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/")
    public ModelAndView index() {
        Map<String, Object> body = Map.of(
                "environment", environment);
        return new ModelAndView("index", body);
    }

    @GetMapping("/enrollments")
    public ModelAndView enrollments() {
        Query query = new Query(Criteria.where("status").is(EnrollmentStatus.INITIALIZED.name()));
        Document document = AnnotationUtils.findAnnotation(Enrollment.class, Document.class);
        List<Enrollment> enrollments = mongoTemplate.find(query, Enrollment.class, document.collection());
        Map<String, Object> body = Map.of(
                "enrollments", enrollments,
                "environment", environment
        );

        return new ModelAndView("enrollments", body);

    }

    @GetMapping("/enrollment/{key}")
    public ModelAndView enrollment(@PathVariable("key") String key) {
        Query query = new Query(Criteria.where("key").is(key));
        String url = String.format("%s/tiqr/metadata?enrollment_key=%s", eduIDBaseUrl, key);
        Map metaData = restTemplate.getForEntity(url, Map.class).getBody();

        Document document = AnnotationUtils.findAnnotation(Enrollment.class, Document.class);
        Enrollment enrollment = mongoTemplate.findOne(query, Enrollment.class, document.collection());

        Map<String, Object> body = Map.of(
                "enrollment", enrollment,
                "metadata", metaData,
                "environment", environment
        );
        return new ModelAndView("enrollment", body);
    }

    @GetMapping("/enrolled/{key}")
    public View enrolled(@PathVariable("key") String key) {
        Enrollment enrollment = findEnrollment(key);
        String url = String.format("%s/tiqr/enrollment?enrollment_secret=%s", eduIDBaseUrl, enrollment.getEnrollmentSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("userid", enrollment.getUserID());
        map.add("secret", Challenge.generateSessionKey());
        map.add("language", "en");
        map.add("notificationType","APNS" );
        map.add("notificationAddress", "1234567890");
        map.add("version","2" );
        map.add("operation","register" );

        RequestEntity<Void> request = new RequestEntity(map, headers, HttpMethod.POST, URI.create(url));

        restTemplate.exchange(request, Void.class);

        return new RedirectView("/enrollments");
    }

    private Enrollment findEnrollment(String key) {
        Query query = new Query(Criteria.where("key").is(key));
        Document document = AnnotationUtils.findAnnotation(Enrollment.class, Document.class);
        return mongoTemplate.findOne(query, Enrollment.class, document.collection());

    }

}
