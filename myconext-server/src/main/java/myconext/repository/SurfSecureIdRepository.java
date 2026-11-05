package myconext.repository;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import myconext.model.User;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

import static myconext.tiqr.SURFSecureID.*;

@Repository
public class SurfSecureIdRepository {

    private static final String SURF_SECURE_ID = "surfSecureId";
    private static final String SURF_SECURE_ID_PATH = SURF_SECURE_ID + ".";

    private final MongoTemplate mongoTemplate;

    public SurfSecureIdRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Atomically increments the sms-rate-limit counter of the user and returns the new value.
     * Returns null when the threshold is reached and the reset window has not passed yet, in
     * which case the document is left untouched. Safe to call concurrently from multiple servers:
     * the check and the increment are a single findOneAndUpdate operation.
     */
    public Integer incrementSmsRateLimitIfAllowed(String userId, int threshold, long resetMinutes) {
        long now = System.currentTimeMillis();
        long resetMillis = resetMinutes * 60_000L;
        Object id = ObjectId.isValid(userId) ? new ObjectId(userId) : userId;
        Document query = new Document("_id", id).append("$or", List.of(
                new Document(SURF_SECURE_ID_PATH + SMS_RATE_LIMIT, new Document("$exists", false)),
                new Document(SURF_SECURE_ID_PATH + SMS_RATE_LIMIT, new Document("$lt", threshold - 1)),
                new Document(SURF_SECURE_ID_PATH + SMS_RATE_LIMIT_UPDATED, new Document("$exists", false)),
                new Document(SURF_SECURE_ID_PATH + SMS_RATE_LIMIT_UPDATED, new Document("$lt", now - resetMillis))
        ));
        List<Document> pipeline = List.of(new Document("$set", new Document()
                .append(SURF_SECURE_ID_PATH + SMS_RATE_LIMIT, new Document("$cond", Arrays.asList(
                        new Document("$eq", Arrays.asList(
                                new Document("$ifNull", Arrays.asList("$" + SURF_SECURE_ID_PATH + SMS_RATE_LIMIT, null)), null)),
                        1,
                        new Document("$cond", List.of(
                                new Document("$gt", List.of(
                                        new Document("$subtract", List.of(now,
                                                new Document("$ifNull", Arrays.asList("$" + SURF_SECURE_ID_PATH + SMS_RATE_LIMIT_UPDATED, 0L)))),
                                        resetMillis)),
                                0,
                                new Document("$add", List.of("$" + SURF_SECURE_ID_PATH + SMS_RATE_LIMIT, 1))
                        ))
                )))
                .append(SURF_SECURE_ID_PATH + SMS_RATE_LIMIT_UPDATED, now)
        ));
        FindOneAndUpdateOptions options = new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
        return mongoTemplate.execute(User.class, collection -> {
            Document result = collection.findOneAndUpdate(query, pipeline, options);
            if (result == null) {
                return null;
            }
            Document surfSecureId = result.get(SURF_SECURE_ID, Document.class);
            return surfSecureId.getInteger(SMS_RATE_LIMIT);
        });
    }

    /**
     * Sets only the phone verification fields, leaving the rest of the user document (e.g. the
     * sms-rate-limit counter) untouched, so concurrent updates are not overwritten.
     */
    public void updatePhoneVerification(String userId, String phoneVerification, String phoneNumber, boolean newUnverifiedPhoneNumber) {
        Update update = new Update()
                .set(SURF_SECURE_ID_PATH + PHONE_VERIFICATION_CODE, phoneVerification)
                .unset(SURF_SECURE_ID_PATH + RATE_LIMIT)
                .unset(SURF_SECURE_ID_PATH + RECOVERY_CODE)
                .set(SURF_SECURE_ID_PATH + (newUnverifiedPhoneNumber ? NEW_UNVERIFIED_PHONE_NUMBER : PHONE_NUMBER), phoneNumber);
        mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(userId)), update, User.class);
    }
}
