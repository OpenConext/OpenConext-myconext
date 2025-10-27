package myconext.mongo;

import com.mongodb.client.MongoClient;
import org.bson.Document;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class ExtendedMongoHealthIndicator implements HealthIndicator {

    private final MongoClient mongoClient;

    public ExtendedMongoHealthIndicator(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @Override
    public Health health() {
        try {
            // Ping MongoDB to verify connectivity
            mongoClient.getDatabase("admin").runCommand(new Document("ping", 1));

            // Retrieve server status info
            Document serverStatus = mongoClient.getDatabase("admin")
                    .runCommand(new Document("serverStatus", 1));

            String host = serverStatus.getString("host");
            String version = serverStatus.getString("version");
            Double uptime = serverStatus.getDouble("uptime");
            Document connections = (Document) serverStatus.get("connections");

            // Optionally, get the default database name
            String databaseName = mongoClient.listDatabaseNames().first();

            return Health.up()
                    .withDetail("host", host)
                    .withDetail("version", version)
                    .withDetail("uptimeSeconds", uptime)
                    .withDetail("connections", connections)
                    .withDetail("defaultDatabase", databaseName)
                    .withDetail("maxWireVersion", getMaxWireVersion())
                    .build();

        } catch (Exception e) {
            return Health.down(e).build();
        }
    }

    private int getMaxWireVersion() {
        try {
            Document buildInfo = mongoClient.getDatabase("admin")
                    .runCommand(new Document("buildInfo", 1));
            return buildInfo.getInteger("maxWireVersion", -1);
        } catch (Exception e) {
            return -1;
        }
    }}

