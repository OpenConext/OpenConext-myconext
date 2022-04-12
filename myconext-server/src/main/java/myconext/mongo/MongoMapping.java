package myconext.mongo;

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.SpringDataMongo3Driver;
import com.github.cloudyrock.spring.v5.MongockSpring5;
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
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;
import org.springframework.data.mongodb.core.query.Collation;

import java.util.Locale;

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
    public MongockSpring5.MongockApplicationRunner mongockApplicationRunner(ApplicationContext springContext,
                                                                            MongoTemplate mongoTemplate) {
        SpringDataMongo3Driver driver = SpringDataMongo3Driver.withDefaultLock(mongoTemplate);
        driver.disableTransaction();

        return MongockSpring5.builder()
                .setDriver(driver)
                .addChangeLogsScanPackage("myconext.mongo")
                .setSpringContext(springContext)
                .buildApplicationRunner();
    }

    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    @SuppressWarnings("unchecked")
    public void initIndicesAfterStartup() {
        ((MappingMongoConverter) mongoConverter).setMapKeyDotReplacement("@");
        MongoMappingContext mappingContext = (MongoMappingContext) this.mongoConverter.getMappingContext();

        for (MongoPersistentEntity<?> persistentEntity : mappingContext.getPersistentEntities()) {
            Class clazz = persistentEntity.getType();
            if (clazz.isAnnotationPresent(Document.class)) {
                MongoPersistentEntityIndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
                IndexOperations indexOps = mongoTemplate.indexOps(clazz);
                resolver.resolveIndexFor(clazz).forEach(indexOps::ensureIndex);
            }
        }

        mongoTemplate.indexOps(User.class).ensureIndex(
                new Index("email", Sort.Direction.ASC).collation(Collation.of(Locale.ENGLISH).strength(2)));
        mongoTemplate.indexOps(EmailsSend.class).ensureIndex(
                new Index("email", Sort.Direction.ASC).collation(Collation.of(Locale.ENGLISH).strength(2)));
    }

}
