package myconext.mongo;

import com.github.mongobeej.Mongobee;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.BasicMongoPersistentEntity;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoMapping {

    private final MongoTemplate mongoTemplate;
    private final MongoConverter mongoConverter;
    private final String mongoUri;

    @Autowired
    public MongoMapping(MongoTemplate mongoTemplate, MongoConverter mongoConverter,
                        @Value("${spring.data.mongodb.uri}") String mongoUri) {
        this.mongoTemplate = mongoTemplate;
        this.mongoConverter = mongoConverter;
        this.mongoUri = mongoUri;
    }

    @Bean
    public MongoTransactionManager transactionManager() {
        return new MongoTransactionManager(mongoTemplate.getMongoDbFactory());
    }

    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    @SuppressWarnings("unchecked")
    public void initIndicesAfterStartup() {
        ((MappingMongoConverter) mongoConverter).setMapKeyDotReplacement("@");
        MongoMappingContext mappingContext = (MongoMappingContext) this.mongoConverter.getMappingContext();

        for (BasicMongoPersistentEntity<?> persistentEntity : mappingContext.getPersistentEntities()) {
            Class clazz = persistentEntity.getType();
            if (clazz.isAnnotationPresent(Document.class)) {
                MongoPersistentEntityIndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);
                IndexOperations indexOps = mongoTemplate.indexOps(clazz);
                resolver.resolveIndexFor(clazz).forEach(indexOps::ensureIndex);
            }
        }
        Mongobee mongobee = new Mongobee(new MongoClientURI(mongoUri));
        mongobee.setChangeLogsScanPackage(this.getClass().getPackage().getName());
        mongobee.execute();
    }

}
