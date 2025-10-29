package myconext.mongo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExtendedMongoHealthIndicator implements HealthIndicator {

    private static final Log LOG = LogFactory.getLog(ExtendedMongoHealthIndicator.class);

    private final MongoTemplate mongoTemplate;

    private final List<String> attributes = List.of(
            "maxWireVersion", "hosts", "isWritablePrimary", "secondary", "primary", "logicalSessionTimeoutMinutes"
    );

    public ExtendedMongoHealthIndicator(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Health health() {
        try {
            Document result = this.mongoTemplate.executeCommand("{ hello: 1 }");
            Health.Builder builder = Health.up();
            result.entrySet().stream()
                    .filter(entry -> attributes.contains(entry.getKey()))
                    .forEach(entry -> builder.withDetail(entry.getKey(), entry.getValue()));
            return builder.up()
                    .build();
        } catch (Exception e) {
            LOG.error("Unhealthy mongoDB", e);
            return Health.down(e).build();
        }
    }

}