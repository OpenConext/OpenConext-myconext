package myconext.repository;


import myconext.model.LinkAccountRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface LinkAccountRequestRepository extends MongoRepository<LinkAccountRequest, String> {

    Optional<LinkAccountRequest> findByHash(String hash);

    Long deleteByExpiresInBefore(Date expiryDate);

}
