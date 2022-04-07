package myconext.repository;


import myconext.model.Challenge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tiqr.org.model.Enrollment;

import java.util.Optional;

@Repository
public interface EnrollmentRepository extends MongoRepository<Enrollment, String>, tiqr.org.repo.EnrollmentRepository {


    Optional<Enrollment> findEnrollmentByUserID(String userID);

}
