package tiqr.org;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AuthorizationEndpoint {

    private static final Log LOG = LogFactory.getLog(AuthorizationEndpoint.class);

    private final String environment;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public AuthorizationEndpoint(MongoTemplate mongoTemplate,
                                 @Value("${environment}") String environment) {

        this.mongoTemplate = mongoTemplate;
        this.environment = environment;
    }

    @GetMapping("/tiqr")
    public ModelAndView authorize(@RequestParam MultiValueMap<String, String> parameters) {
        Map<String, Object> body = new HashMap<>();
        body.put("parameters", parameters.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().get(0)
        )));
        body.put("environment", environment);
        return new ModelAndView("index", body);

    }
}
