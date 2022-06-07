package myconext.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tiqr.org.model.Enrollment;
import tiqr.org.model.EnrollmentStatus;

import java.time.Instant;
import java.util.List;

@Repository
public interface EnrollmentRepository extends MongoRepository<Enrollment, String>, tiqr.org.repo.EnrollmentRepository {

    Long deleteByUpdatedBefore(Instant instant);
}
