package myconext.cron;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import myconext.AbstractIntegrationTest;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;


class AbstractNodeLeaderTest extends AbstractIntegrationTest {

    @Autowired
    private MongoClient mongoClient;

    private static final String TEST_DATABASE = "test_db";
    private static final String LOCK_COLLECTION = "distributed_locks";
    private static final String TEST_LOCK_NAME = "test-cron-lock";

    private TestNodeLeader nodeLeader;

    @BeforeEach
    void setUp() {
        nodeLeader = new TestNodeLeader(TEST_LOCK_NAME, mongoClient, TEST_DATABASE);
        cleanupLocks();
    }

    @AfterEach
    void tearDown() {
        cleanupLocks();
    }

    private void cleanupLocks() {
        MongoDatabase database = mongoClient.getDatabase(TEST_DATABASE);
        MongoCollection<Document> collection = database.getCollection(LOCK_COLLECTION);
        collection.deleteMany(new Document());
    }

    @Test
    void testSingleNodeAcquiresLockSuccessfully() {
        AtomicInteger executionCount = new AtomicInteger(0);

        nodeLeader.perform("TestJob", executionCount::incrementAndGet);

        assertEquals(1, executionCount.get(), "Job should execute once");
    }

    @Test
    void testMultipleNodesOnlyOneExecutes() throws InterruptedException {
        int numberOfNodes = 5;
        AtomicInteger executionCount = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numberOfNodes);
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfNodes);

        for (int i = 0; i < numberOfNodes; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    TestNodeLeader leader = new TestNodeLeader(TEST_LOCK_NAME, mongoClient, TEST_DATABASE);
                    leader.perform("ConcurrentJob", () -> {
                        executionCount.incrementAndGet();
                        Thread.sleep(100); // Simulate some work
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        startLatch.countDown(); // Start all threads simultaneously
        assertTrue(completionLatch.await(10, TimeUnit.SECONDS), "All threads should complete");
        executorService.shutdown();

        assertEquals(1, executionCount.get(), "Only one node should execute the job");
    }

    @Test
    void testDifferentLocksCanRunSimultaneously() throws InterruptedException {
        AtomicInteger job1Count = new AtomicInteger(0);
        AtomicInteger job2Count = new AtomicInteger(0);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(2);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        executorService.submit(() -> {
            try {
                startLatch.await();
                TestNodeLeader leader1 = new TestNodeLeader("lock-1", mongoClient, TEST_DATABASE);
                leader1.perform("Job1", () -> {
                    job1Count.incrementAndGet();
                    Thread.sleep(100);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                completionLatch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                startLatch.await();
                TestNodeLeader leader2 = new TestNodeLeader("lock-2", mongoClient, TEST_DATABASE);
                leader2.perform("Job2", () -> {
                    job2Count.incrementAndGet();
                    Thread.sleep(100);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                completionLatch.countDown();
            }
        });

        startLatch.countDown();
        assertTrue(completionLatch.await(10, TimeUnit.SECONDS), "Both jobs should complete");
        executorService.shutdown();

        assertEquals(1, job1Count.get(), "Job 1 should execute once");
        assertEquals(1, job2Count.get(), "Job 2 should execute once");
    }

    @Test
    void testLockIsReleasedAfterExecution() {
        AtomicInteger executionCount = new AtomicInteger(0);

        // First execution
        nodeLeader.perform("TestJob", executionCount::incrementAndGet);
        assertEquals(1, executionCount.get());

        // Second execution - should succeed because lock was released
        nodeLeader.perform("TestJob", executionCount::incrementAndGet);
        assertEquals(2, executionCount.get(), "Lock should be released after first execution");
    }

    @Test
    void testLockIsReleasedEvenWhenExecutionFails() {
        AtomicInteger executionCount = new AtomicInteger(0);

        // First execution that throws exception
        nodeLeader.perform("FailingJob", () -> {
            executionCount.incrementAndGet();
            throw new RuntimeException("Simulated failure");
        });
        assertEquals(1, executionCount.get());

        // Second execution - should succeed because lock was released despite exception
        nodeLeader.perform("FailingJob", executionCount::incrementAndGet);
        assertEquals(2, executionCount.get(), "Lock should be released even after exception");
    }

    @Test
    void testStaleLockCanBeAcquiredByAnotherNode() {
        MongoDatabase database = mongoClient.getDatabase(TEST_DATABASE);
        MongoCollection<Document> collection = database.getCollection(LOCK_COLLECTION);

        // Manually insert a stale lock (expired 10 minutes ago)
        Instant tenMinutesAgo = Instant.now().minusSeconds(600);
        Document staleLock = new Document()
                .append("_id", TEST_LOCK_NAME)
                .append("nodeId", "dead-node-123")
                .append("acquiredAt", Date.from(tenMinutesAgo))
                .append("expiresAt", Date.from(tenMinutesAgo.plusSeconds(300)));
        collection.insertOne(staleLock);

        AtomicInteger executionCount = new AtomicInteger(0);

        // New node should be able to acquire the stale lock
        nodeLeader.perform("TestJob", executionCount::incrementAndGet);

        assertEquals(1, executionCount.get(), "Job should execute despite stale lock");

        // Verify the new lock was deleted
        Document updatedLock = collection.find(new Document("_id", TEST_LOCK_NAME)).first();
        assertNull(updatedLock);
    }

    @Test
    void testSequentialExecutionsWithSameLock() throws InterruptedException {
        int numberOfExecutions = 10;
        AtomicInteger executionCount = new AtomicInteger(0);
        List<Integer> executionOrder = new ArrayList<>();

        for (int i = 0; i < numberOfExecutions; i++) {
            final int executionNumber = i;
            nodeLeader.perform("SequentialJob", () -> {
                executionCount.incrementAndGet();
                executionOrder.add(executionNumber);
                Thread.sleep(10);
            });
        }

        assertEquals(numberOfExecutions, executionCount.get(),
                "All sequential executions should complete");
        assertEquals(numberOfExecutions, executionOrder.size(),
                "Execution order should be recorded");
    }

    @Test
    void testLockDocumentStructure() {
        nodeLeader.perform("TestJob", () -> {
            // Check lock structure while it's held
            MongoDatabase database = mongoClient.getDatabase(TEST_DATABASE);
            MongoCollection<Document> collection = database.getCollection(LOCK_COLLECTION);
            Document lock = collection.find(new Document("_id", TEST_LOCK_NAME)).first();

            assertNotNull(lock, "Lock document should exist");
            assertEquals(TEST_LOCK_NAME, lock.getString("_id"));
            assertNotNull(lock.getString("nodeId"), "Lock should have nodeId");
            assertNotNull(lock.getDate("acquiredAt"), "Lock should have acquiredAt timestamp");
            assertNotNull(lock.getDate("expiresAt"), "Lock should have expiresAt timestamp");
        });
    }

    // Test implementation of AbstractNodeLeader
    static class TestNodeLeader extends AbstractNodeLeader {
        public TestNodeLeader(String lockName, MongoClient mongoClient, String databaseName) {
            super(lockName, mongoClient, databaseName, true);
        }
    }

}