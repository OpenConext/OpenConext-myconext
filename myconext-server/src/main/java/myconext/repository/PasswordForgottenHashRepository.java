package myconext.repository;


import myconext.model.Challenge;
import myconext.model.PasswordForgottenHash;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PasswordForgottenHashRepository extends MongoRepository<PasswordForgottenHash, String> {

    Optional<PasswordForgottenHash> findByHashAndUserId(String hash, String userId);

    List<PasswordForgottenHash> findByUserId(String userId);

    Long deleteByExpiresInBefore(Date expiryDate);

}
