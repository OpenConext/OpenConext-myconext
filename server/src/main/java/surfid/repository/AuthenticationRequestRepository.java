package surfid.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import surfid.model.SamlAuthenticationRequest;

import java.util.Date;

@Repository
public interface AuthenticationRequestRepository extends MongoRepository<SamlAuthenticationRequest, String> {

    Long deleteByExpiresInBefore(Date expiryDate);
}
