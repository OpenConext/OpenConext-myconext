package myconext.repository;


import myconext.model.ExternalUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExternalUserRepository extends MongoRepository<ExternalUser, String> {

    Optional<ExternalUser> findUserByUid(String uid);

}
