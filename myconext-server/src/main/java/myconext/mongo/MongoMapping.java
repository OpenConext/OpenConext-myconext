package myconext.mongo;

import io.mongock.driver.mongodb.springdata.v4.SpringDataMongoV4Driver;
import io.mongock.runner.springboot.MongockSpringboot;
import io.mongock.runner.springboot.base.MongockApplicationRunner;
import lombok.SneakyThrows;
import myconext.model.EmailsSend;
import myconext.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.query.Collation;
import tiqr.org.model.Authentication;
import tiqr.org.model.Enrollment;
import tiqr.org.model.Registration;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Configuration
public class MongoMapping {

    private final MongoTemplate mongoTemplate;
    private final MongoConverter mongoConverter;

    @Autowired
    public MongoMapping(MongoTemplate mongoTemplate, MongoConverter mongoConverter) {
        this.mongoTemplate = mongoTemplate;
        this.mongoConverter = mongoConverter;
    }

    @Bean
    public MongoTransactionManager transactionManager() {
        return new MongoTransactionManager(mongoTemplate.getMongoDatabaseFactory());
    }

    @Bean
    public MongockApplicationRunner mongockApplicationRunner(ApplicationContext springContext,
                                                             MongoTemplate mongoTemplate) {
        SpringDataMongoV4Driver driver = SpringDataMongoV4Driver.withDefaultLock(mongoTemplate);
        driver.disableTransaction();
        return MongockSpringboot.builder()
                .setDriver(driver)
                .addMigrationScanPackage("myconext.mongo")
                .setSpringContext(springContext)
                .buildApplicationRunner();
    }

    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    @SuppressWarnings("unchecked")
    public void initIndicesAfterStartup() {
        ((MappingMongoConverter) mongoConverter).setMapKeyDotReplacement("@");
        MongoMappingContext mappingContext = (MongoMappingContext) this.mongoConverter.getMappingContext();

        Collection<MongoPersistentEntity<?>> persistentEntities = mappingContext.getPersistentEntities();
        for (MongoPersistentEntity<?> persistentEntity : persistentEntities) {
            Class clazz = persistentEntity.getType();
            if (clazz.isAnnotationPresent(Document.class)) {
                MongoPersistentEntityIndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
                IndexOperations indexOps = mongoTemplate.indexOps(clazz);
                resolver.resolveIndexFor(clazz).forEach(indexOps::ensureIndex);
            }
        }
        //Avoid command failed with error 86 (IndexKeySpecsConflict): An existing index has the same name as the requested index.
        List<String> userIndexes = mongoTemplate.indexOps(User.class)
                .getIndexInfo().stream()
                .map(IndexInfo::getName)
                .filter(name -> name.contains("email"))
                .collect(Collectors.toList());
        String userEmailUniqueIndexName = "user_email_unique";
        //Drop existing indexes on email, but not the new one
        userIndexes.stream()
                .filter(name -> !name.equals(userEmailUniqueIndexName))
                .forEach(name -> mongoTemplate.indexOps(User.class).dropIndex(name));
        //Create - if not already exists - an email case-insensitive unique index
        if (!userIndexes.contains(userEmailUniqueIndexName)) {
            mongoTemplate.indexOps(User.class).ensureIndex(
                    new Index("email", Sort.Direction.ASC)
                            .named(userEmailUniqueIndexName)
                            .collation(Collation.of(Locale.ENGLISH).strength(2))
                            .unique());
        }

        //tiqr
        mongoTemplate.indexOps(Enrollment.class).ensureIndex(
                new Index("key", Sort.Direction.ASC));
        mongoTemplate.indexOps(Enrollment.class).ensureIndex(
                new Index("enrollmentSecret", Sort.Direction.ASC));
        mongoTemplate.indexOps(Authentication.class).ensureIndex(
                new Index("sessionKey", Sort.Direction.ASC));
        IndexOperations registrationsIndex = mongoTemplate.indexOps(Registration.class);
        if (registrationsIndex.getIndexInfo().stream().anyMatch(indexInfo -> indexInfo.getName().equals("userid"))) {
            registrationsIndex.dropIndex("userid");
        }
        registrationsIndex.ensureIndex(
                new Index("userId", Sort.Direction.ASC));
        mongoTemplate.indexOps(EmailsSend.class).ensureIndex(
                new Index("email", Sort.Direction.ASC).collation(Collation.of(Locale.ENGLISH).strength(2)));
    }

}
