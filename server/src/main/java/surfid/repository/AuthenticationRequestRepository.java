package surfid.repository;


import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import surfid.exceptions.ExpiredAuthenticationException;
import surfid.model.SamlAuthenticationRequest;

import java.util.Date;
import java.util.Optional;

@Repository
public interface AuthenticationRequestRepository extends MongoRepository<SamlAuthenticationRequest, String> {

    Long deleteByExpiresInBefore(Date expiryDate);

    Optional<SamlAuthenticationRequest> findByHash(String hash);

    default Optional<SamlAuthenticationRequest> findByIdAndNotExpired(String id) {
        Optional<SamlAuthenticationRequest> authenticationRequestOptional = findById(id);
        authenticationRequestOptional.ifPresent(req -> {
            if (req.isExpired()) {
                throw new ExpiredAuthenticationException();
            }
        });
        return authenticationRequestOptional;
    }
}
