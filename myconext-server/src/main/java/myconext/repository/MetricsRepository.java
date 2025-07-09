package myconext.repository;

import com.mongodb.client.AggregateIterable;
import myconext.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.bson.Document;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public class MetricsRepository {

    private final MongoTemplate mongoTemplate;

    public MetricsRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Number getTotalLinkedAccountCount() {
        UnwindOperation unwindLinkedAccounts = Aggregation.unwind("linkedAccounts");
        CountOperation countTotal = Aggregation.count().as("totalLinkedAccounts");
        TypedAggregation<User> aggregation = Aggregation.newAggregation(
                User.class,
                unwindLinkedAccounts,
                countTotal
        );
        AggregationResults<Map> results = mongoTemplate.aggregate(
                aggregation,
                Map.class
        );
        return (Number) results.getUniqueMappedResult().get("totalLinkedAccounts");
    }

    // Alternative approach: Using MongoTemplate's native execute method
    public Number countTotalLinkedAccountsNativeExecute() {
        /*
            users.aggregate([
                  {
                    "$unwind": "$linkedAccounts"
                  },
                  {
                    "$count": "totalLinkedAccounts"
                  }
                ])

         */
        return mongoTemplate.execute("users", collection -> {
            List<Document> pipeline = Arrays.asList(
                    Document.parse("{ \"$unwind\": \"$linkedAccounts\" }"),
                    Document.parse("{ \"$count\": \"totalLinkedAccounts\" }")
            );

            AggregateIterable<Document> result = collection.aggregate(pipeline);
            Document doc = result.first();

            return doc != null ? doc.getInteger("totalLinkedAccounts") : 0;
        });
    }
}
