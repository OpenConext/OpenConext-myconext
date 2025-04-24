package myconext.repository;


import myconext.model.LoginStatus;
import myconext.model.RequestInstitutionEduID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface RequestInstitutionEduIDRepository extends MongoRepository<RequestInstitutionEduID, String> {

    Optional<RequestInstitutionEduID> findByHash(String hash);

    Long deleteByExpiresInBefore(Date expiryDate);

}
