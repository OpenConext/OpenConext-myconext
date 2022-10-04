package myconext.repository;


import myconext.model.PasswordResetHash;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordResetHashRepository extends MongoRepository<PasswordResetHash, String> {

    Optional<PasswordResetHash> findByHashAndUserId(String hash, String userId);

    List<PasswordResetHash> findByUserId(String userId);

    Long deleteByUserId(String userId);

    Long deleteByExpiresInBefore(Date expiryDate);

}
