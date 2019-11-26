package myconext.repository;


import myconext.exceptions.ExpiredAuthenticationException;
import myconext.model.SamlAuthenticationRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface AuthenticationRequestRepository extends MongoRepository<SamlAuthenticationRequest, String> {

    Long deleteByExpiresInBeforeAndRememberMe(Date expiryDate, boolean rememberMe);

    Optional<SamlAuthenticationRequest> findByHash(String hash);

    default Optional<SamlAuthenticationRequest> findByIdAndNotExpired(String id) {
        Optional<SamlAuthenticationRequest> authenticationRequestOptional = findById(id);
        authenticationRequestOptional.ifPresent(req -> {
            if (req.isExpired() && !req.isRememberMe()) {
                throw new ExpiredAuthenticationException();
            }
        });
        return authenticationRequestOptional;
    }
}
