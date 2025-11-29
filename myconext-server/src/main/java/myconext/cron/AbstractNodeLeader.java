package myconext.cron;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReturnDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;

import java.time.Instant;
import java.util.Date;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public abstract class AbstractNodeLeader {

    private static final Log LOG = LogFactory.getLog(AbstractNodeLeader.class);
    private static final String LOCK_COLLECTION = "distributed_locks";
    private static final int LOCK_TIMEOUT_SECONDS = 300; // 5 minutes stale lock timeout

    private final String lockName;
    private final MongoClient mongoClient;
    private final String databaseName;

    protected AbstractNodeLeader(String lockName, MongoClient mongoClient, String databaseName) {
        this.lockName = lockName;
        this.mongoClient = mongoClient;
        this.databaseName = databaseName;
        ensureIndexes();
    }

    private void ensureIndexes() {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(LOCK_COLLECTION);

            // Create TTL index on expiresAt field to automatically clean up stale locks
            IndexOptions indexOptions = new IndexOptions();
            Document indexDocument = new Document("expiresAt", 1);
            collection.createIndex(indexDocument, indexOptions.expireAfter(0L, java.util.concurrent.TimeUnit.SECONDS));
        } catch (Exception e) {
            LOG.warn("Failed to create indexes for distributed locks", e);
        }
    }

    public void perform(String name, Executable executable) {
        String nodeId = null;
        boolean lockAcquired = false;

        try {
            nodeId = generateNodeId();
            lockAcquired = tryGetLock(lockName, nodeId);

            if (!lockAcquired) {
                LOG.info(String.format("Another node is running %s, skipping this one", name));
                return;
            }

            LOG.info(String.format("Lock acquired for %s", name));
            executable.execute();
            LOG.info(String.format("Executable %s completed successfully", name));
        } catch (Throwable e) {
            LOG.error(String.format("Error occurred in %s", name), e);
        } finally {
            if (lockAcquired && nodeId != null) {
                try {
                    releaseLock(lockName, nodeId);
                    LOG.info(String.format("Lock released for %s", name));
                } catch (Exception e) {
                    LOG.error(String.format("Failed to release lock %s", name), e);
                }
            }
        }
    }

    protected boolean tryGetLock(String name, String nodeId) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(LOCK_COLLECTION);

            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(LOCK_TIMEOUT_SECONDS);

            // First, try to insert a new lock document (for when lock doesn't exist)
            try {
                Document lockDoc = new Document()
                        .append("_id", name)
                        .append("nodeId", nodeId)
                        .append("acquiredAt", Date.from(now))
                        .append("expiresAt", Date.from(expiresAt));

                collection.insertOne(lockDoc);
                return true; // Successfully inserted = lock acquired
            } catch (MongoWriteException e) {
                // Lock already exists (duplicate key error), try to update if expired
                if (e.getCode() == 11000) { // Duplicate key error
                    // Try to acquire lock only if it's expired
                    Document result = collection.findOneAndUpdate(
                            and(
                                    eq("_id", name),
                                    lt("expiresAt", Date.from(now))
                            ),
                            combine(
                                    set("nodeId", nodeId),
                                    set("acquiredAt", Date.from(now)),
                                    set("expiresAt", Date.from(expiresAt))
                            ),
                            new FindOneAndUpdateOptions()
                                    .returnDocument(ReturnDocument.AFTER)
                    );

                    // Check if we successfully updated an expired lock
                    return result != null && nodeId.equals(result.getString("nodeId"));
                } else {
                    throw e; // Rethrow if it's a different error
                }
            }
        } catch (Exception e) {
            LOG.error(String.format("Failed to acquire lock %s", name), e);
            return false;
        }
    }

    protected void releaseLock(String name, String nodeId) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(LOCK_COLLECTION);

            // Only release if we own the lock
            collection.deleteOne(
                    and(
                            eq("_id", name),
                            eq("nodeId", nodeId)
                    )
            );
        } catch (Exception e) {
            LOG.error(String.format("Failed to release lock %s", name), e);
        }
    }

    protected String generateNodeId() {
        // Generate unique node identifier
        return String.format("%s-%s-%d",
                getHostName(),
                Thread.currentThread().getName(),
                System.currentTimeMillis()
        );
    }

    private String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }
}