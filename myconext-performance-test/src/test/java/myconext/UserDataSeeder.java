package myconext;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertManyOptions;
import net.datafaker.Faker;
import org.bson.Document;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

public class UserDataSeeder {

    // ---- Database configuration ----
    private static final String MONGO_URI = System.getProperty("dbUrl", "mongodb://localhost:27017");
    private static final String DATABASE_NAME = System.getProperty("dbName", "myconext_performance");
    private static final String USERS_COLLECTION = System.getProperty("usersCollection", "users");

    // ---- Seeding configuration ----
    private static final int TOTAL_USERS = 1_000_000;
    private static final int BATCH_SIZE = 5_000;

    public static void main(String[] args) {
        Faker faker = new Faker(new Random(42)); // deterministic

        try (MongoClient client = MongoClients.create(MONGO_URI)) {

            MongoDatabase db = client.getDatabase(DATABASE_NAME);
            MongoCollection<Document> users = db.getCollection(USERS_COLLECTION);

            // Clear collection before seeding
            users.deleteMany(new Document());
            System.out.println("Cleared collection: " + USERS_COLLECTION);

            List<Document> batch = new ArrayList<>(BATCH_SIZE);
            for (int i = 1; i <= TOTAL_USERS; i++) {
                Document user = buildUser(faker);
                batch.add(user);

                if (batch.size() == BATCH_SIZE) {
                    users.insertMany(batch, new InsertManyOptions().ordered(false));
                    batch.clear();
                    System.out.println("Inserted users: " + i);
                }
            }

            if (!batch.isEmpty()) {
                users.insertMany(batch, new InsertManyOptions().ordered(false));
            }

            System.out.println("✅ User seeding completed");
        }
    }

    private static Document buildUser(Faker faker) {

        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofDays(6L * 365));

        LocalDate birthday = LocalDate.now(ZoneOffset.UTC)
                .minusYears(faker.number().numberBetween(18, 70))
                .minusDays(faker.number().numberBetween(0, 365));

        Document issuer = new Document()
                .append("_id", "studielink")
                .append("name", "studielink");

        Document externalLinkedAccount = new Document()
                .append("subjectId", UUID.randomUUID().toString())
                .append("idpScoping", "studielink")
                .append("issuer", issuer)
                .append("verification", "Geverifieerd")
                .append("subjectIssuer", "studielink")
                .append("brinCodes", List.of("ST42"))
                .append("affiliations", List.of("student@aap.nl"))
                .append("external", true)
                .append("preferred", true)
                .append("createdAt", Date.from(now))
                .append("expiresAt", Date.from(expiresAt));

        Document eduIdService = new Document()
                .append("name", "studielink")
                .append("nameNl", "studielink")
                .append("institutionGuid", "ec9d6d75-0d11-e511-80d0-005056956c1a")
                .append("logoUrl", "https://static.surfconext.nl/logos/org/ec9d6d75-0d11-e511-80d0-005056956c1a.png")
                .append("createdAt", Date.from(now))
                .append("lastLogin", Date.from(now));

        Document eduId = new Document()
                .append("value", UUID.randomUUID().toString())
                .append("createdAt", Date.from(now))
                .append("services", List.of(eduIdService));

        return new Document()
                .append("_class", "myconext.model.User")
                .append("uid", UUID.randomUUID().toString())
                .append("email", "user-" + UUID.randomUUID() + "@test.com")
                .append("givenName", faker.name().firstName())
                .append("chosenName", faker.name().firstName())
                .append("familyName", faker.name().lastName())
                .append("preferredLanguage", "en")
                .append("created", now.getEpochSecond())
                .append("lastLogin", 0)
                .append("newUser", false)
                .append("forgottenPassword", false)
                .append("rateLimited", false)
                .append("serviceDeskMember", false)
                .append("schacHomeOrganization", "eduid.nl")
                .append("dateOfBirth",
                        Date.from(birthday.atStartOfDay(ZoneOffset.UTC).toInstant()))
                .append("eduIDS", List.of(eduId))
                .append("externalLinkedAccounts", List.of(externalLinkedAccount))
                .append("linkedAccounts", List.of())
                .append("publicKeyCredentials", List.of())
                .append("attributes", new Document())
                .append("surfSecureId", new Document());
    }
}
