package myconext.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tiqr.org.model.Enrollment;

import java.time.Instant;

@Repository
public interface EnrollmentRepository extends MongoRepository<Enrollment, String>, tiqr.org.repo.EnrollmentRepository {

    Long deleteByUpdatedBefore(Instant instant);
}
