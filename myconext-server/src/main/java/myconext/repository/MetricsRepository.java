package myconext.repository;

import com.mongodb.client.AggregateIterable;
import myconext.model.IdpScoping;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public Integer countTotalAppRegistrations() {
        return doInCollection("registrations",
                List.of(
                        "{ \"$count\": \"totalAppRegistrations\" }"
                ), "totalAppRegistrations");
    }

    public Integer countTotalExternalLinkedAccountsByType(IdpScoping idpScoping) {
        return doInCollection("users",
                List.of(
                        "{ \"$unwind\": \"$externalLinkedAccounts\" }",
                        "{ \"$match\": { \"externalLinkedAccounts.idpScoping\": \"" + idpScoping.name() + "\" } },",
                        "{ \"$count\": \"countExternalLinkedAccounts\" }"
                ), "countExternalLinkedAccounts");
    }

    public Map<String, Integer> countExternalLinkedAccountsByBrin() {
        return mongoTemplate.execute("users", collection -> {
            List<Document> pipeline = List.of(
                    Document.parse("{ \"$unwind\": \"$externalLinkedAccounts\" }"),
                    Document.parse("{ \"$unwind\": \"$externalLinkedAccounts.brinCodes\" }"),
                    Document.parse("{ \"$group\": { \"_id\": \"$externalLinkedAccounts.brinCodes\", \"count\": { \"$sum\": 1 } } }")
            );
            AggregateIterable<Document> result = collection.aggregate(pipeline);
            Map<String, Integer> countsByBrin = new LinkedHashMap<>();
            result.forEach(doc -> countsByBrin.put(doc.getString("_id"), doc.getInteger("count")));
            return countsByBrin;
        });
    }

    public Integer countTotalExternalLinkedAccounts() {
        return doInCollection("users",
                List.of(
                        "{ \"$unwind\": \"$externalLinkedAccounts\" }",
                        "{ \"$count\": \"totalExternalLinkedAccounts\" }"
                ), "totalExternalLinkedAccounts");
    }

    public Integer countTotalUsedServices() {
        return doInCollection("users",
                List.of(
                        "{ \"$unwind\": \"$eduIDS\" }",
                        "{ \"$unwind\": \"$eduIDS.services\" }",
                        "{ \"$group\": { \"_id\": \"$eduIDS.services.entityId\" } },",
                        "{ \"$count\": \"countTotalUsedServices\" }"
                ), "countTotalUsedServices");
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
