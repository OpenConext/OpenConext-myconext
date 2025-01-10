package myconext.repository;


import myconext.model.SamlAuthenticationRequest;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthenticationRequestRepository extends MongoRepository<SamlAuthenticationRequest, String> {

    Long deleteByExpiresInBeforeAndRememberMe(Date expiryDate, boolean rememberMe);

    Optional<SamlAuthenticationRequest> findByHash(String hash);

    Optional<SamlAuthenticationRequest> findByRememberMeValue(String rememberMeValue);

    List<SamlAuthenticationRequest> findByUserIdAndRememberMe(String userId, boolean rememberMe);

    Long deleteByUserId(String userId);

    default Optional<SamlAuthenticationRequest> findByIdAndNotExpired(String id) {
        Optional<SamlAuthenticationRequest> authenticationRequestOptional = findById(id);
        if (authenticationRequestOptional.isPresent()) {
            SamlAuthenticationRequest req = authenticationRequestOptional.get();
            if (new Date().after(req.getExpiresIn()) && !req.isRememberMe()) {
                return Optional.empty();
            }
        }
        return authenticationRequestOptional;
    }
}
