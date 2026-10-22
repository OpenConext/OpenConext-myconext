package myconext.e2e;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.model.User;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Seeds the user the LocalDevelopmentAuthenticationFilter (profile test or local) authenticates as, so that
 * the end-to-end tests also run against an empty database. The seeded users are reset on every start of the server and
 * by the e2e controller, other users are left alone.
 * Start with: --spring.profiles.active=test,e2e
 */
@Component
@Profile("e2e")
public class E2eSeed {

    private static final Log LOG = LogFactory.getLog(E2eSeed.class);

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    public E2eSeed(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    // After the runners, so after the Mongock migrations: they can not handle the seeded users
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() throws IOException {
        seed();
    }

    public void seed() throws IOException {
        List<Map<String, Object>> users = objectMapper.readValue(
                new ClassPathResource("e2e/users.json").getInputStream(), new TypeReference<>() {
                });
        // Same approach as the integration tests: the maps are converted by the User mapping
        mongoTemplate.bulkOps(BulkOperations.BulkMode.ORDERED, User.class)
                .remove(Query.query(Criteria.where("uid").in(users.stream().map(user -> user.get("uid")).toList())))
                .insert(users)
                .execute();
        LOG.info(String.format("Seeded %s users for the e2e tests", users.size()));
    }
}
