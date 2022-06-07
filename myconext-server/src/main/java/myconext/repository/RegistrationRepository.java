package myconext.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tiqr.org.model.Registration;
import tiqr.org.model.RegistrationStatus;

import java.time.Instant;

@Repository
public interface RegistrationRepository extends MongoRepository<Registration, String>, tiqr.org.repo.RegistrationRepository {

    Long deleteByUpdatedBeforeAndStatus(Instant instant, RegistrationStatus status);
}
