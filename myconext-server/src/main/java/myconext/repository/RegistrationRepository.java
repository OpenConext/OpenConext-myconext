package myconext.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import tiqr.org.model.Authentication;
import tiqr.org.model.Registration;

@Repository
public interface RegistrationRepository extends MongoRepository<Registration, String>, tiqr.org.repo.RegistrationRepository {

}
