package myconext.repository;


import myconext.model.MobileLinkAccountRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface MobileLinkAccountRequestRepository extends MongoRepository<MobileLinkAccountRequest, String> {

    Optional<MobileLinkAccountRequest> findByHash(String hash);

    Long deleteByExpiresInBefore(Date expiryDate);

}
