package myconext.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tiqr.org.model.Authentication;

import java.time.Instant;

@Repository
public interface AuthenticationRepository extends MongoRepository<Authentication, String>, tiqr.org.repo.AuthenticationRepository {

    Long deleteByUpdatedBefore(Instant instant);

}
