package surfid.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import surfid.model.AuthenticationRequest;

import java.util.Date;

public interface AuthenticationRequestRepository extends MongoRepository<AuthenticationRequest, String> {

    Long deleteByExpiresInBefore(Date expiryDate);
}
