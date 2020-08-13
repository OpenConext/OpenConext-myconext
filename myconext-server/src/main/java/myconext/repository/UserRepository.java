package myconext.repository;


import myconext.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findUserByUid(String uid);

    Optional<User> findUserByEmailIgnoreCase(String email);

    User findOneUserByEmailIgnoreCase(String email);

    Optional<User> findUserByLinkedAccounts_eduPersonPrincipalName(String eduPersonPrincipalName);

    List<User> findByLinkedAccounts_ExpiresAtBefore(Date expiryDate);

    List<User> findByNewUserTrueAndCreatedLessThan(long millis);

    Optional<User> findUserByWebAuthnIdentifier(String webAuthnIdentifier);

    Optional<User> findUserByUserHandle(String userHandle);
}
