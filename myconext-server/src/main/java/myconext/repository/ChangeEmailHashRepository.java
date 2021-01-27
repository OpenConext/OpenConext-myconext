package myconext.repository;


import myconext.model.ChangeEmailHash;
import myconext.model.PasswordForgottenHash;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface ChangeEmailHashRepository extends MongoRepository<ChangeEmailHash, String> {

    Optional<ChangeEmailHash> findByHashAndUserId(String hash, String userId);

    Long deleteByUserId(String userId);

    Long deleteByExpiresInBefore(Date expiryDate);

}
