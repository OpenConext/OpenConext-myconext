package myconext.repository;

import com.mongodb.client.AggregateIterable;
import myconext.model.IdpScoping;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MetricsRepository {

    private final MongoTemplate mongoTemplate;

    public MetricsRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Integer countTotalLinkedAccounts() {
        return doInCollection("users",
                List.of(
                        "{ \"$unwind\": \"$linkedAccounts\" }",
                        "{ \"$count\": \"totalLinkedAccounts\" }"
                ), "totalLinkedAccounts");
    }

    public Integer countTotalExternalLinkedAccountsByType(IdpScoping idpScoping) {
        return doInCollection("users",
                List.of(
                        "{ \"$unwind\": \"$externalLinkedAccounts\" }",
                        "{ \"$match\": { \"externalLinkedAccounts.idpScoping\": \"" + idpScoping.name() + "\" } },",
                        "{ \"$count\": \"countExternalLinkedAccounts\" }"
                ), "countExternalLinkedAccounts");
    }

    public Integer countTotalRegisteredApps() {
        return doInCollection("users",
                List.of(
                        "{ \"$unwind\": \"$eduIDS\" }",
                        "{ \"$unwind\": \"$eduIDS.services\" }",
                        "{ \"$group\": { \"_id\": \"$eduIDS.services.entityId\" } },",
                        "{ \"$count\": \"countTotalRegisteredApps\" }"
                ), "countTotalRegisteredApps");
    }

    private Integer doInCollection(String collectionName, List<String> pipeLines, String resultKeyWord) {
        return mongoTemplate.execute(collectionName, collection -> {
            List<Document> documents = pipeLines
                    .stream()
                    .map(Document::parse)
                    .toList();
            AggregateIterable<Document> result = collection.aggregate(documents);
            Document doc = result.first();
            return doc != null ? doc.getInteger(resultKeyWord) : 0;
        });

    }
}
