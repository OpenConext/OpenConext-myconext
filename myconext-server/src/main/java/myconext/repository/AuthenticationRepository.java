package myconext.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tiqr.org.model.Authentication;
import tiqr.org.model.AuthenticationStatus;
import tiqr.org.model.Enrollment;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Repository
public interface AuthenticationRepository extends MongoRepository<Authentication, String>, tiqr.org.repo.AuthenticationRepository {

    Long deleteByUpdatedBefore(Instant instant);

}
