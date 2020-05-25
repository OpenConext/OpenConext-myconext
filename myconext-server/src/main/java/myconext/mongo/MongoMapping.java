package myconext.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
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

    @Autowired
    public MongoMapping(MongoTemplate mongoTemplate, MongoConverter mongoConverter) {
        this.mongoTemplate = mongoTemplate;
        this.mongoConverter = mongoConverter;
    }

    @EventListener(ApplicationReadyEvent.class)
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
    }

}
