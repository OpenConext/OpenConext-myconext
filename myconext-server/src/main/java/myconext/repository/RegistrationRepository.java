package myconext.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tiqr.org.model.Registration;
import tiqr.org.model.RegistrationStatus;

import java.time.Instant;
import java.util.List;

@Repository
public interface RegistrationRepository extends MongoRepository<Registration, String>, tiqr.org.repo.RegistrationRepository {

    List<Registration> findByUpdatedBeforeAndStatus(Instant instant, RegistrationStatus status);

    void delete(Registration registration);
}
