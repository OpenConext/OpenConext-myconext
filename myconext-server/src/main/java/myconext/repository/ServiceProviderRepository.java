package myconext.repository;


import myconext.model.Challenge;
import myconext.model.ServiceProvider;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServiceProviderRepository extends MongoRepository<ServiceProvider, String> {

    Optional<ServiceProvider> findByEntityId(String entityId);

}
