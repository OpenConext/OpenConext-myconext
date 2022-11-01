package myconext.repository;


import myconext.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findUserByUid(String uid);

    @Query(collation = "{ 'locale' : 'en_US', 'strength' : 2 }")
    Optional<User> findUserByEmail(String email);

    @Query(collation = "{ 'locale' : 'en_US', 'strength' : 2 }")
    User findOneUserByEmail(String email);

    Optional<User> findUserByLinkedAccounts_eduPersonPrincipalName(String eduPersonPrincipalName);

    List<User> findByLinkedAccounts_ExpiresAtBefore(Date expiryDate);

    List<User> findByLinkedAccounts_EduPersonPrincipalName(String eduPersonPrincipalName);

    Optional<User> findByEduIDS_value(String value);

    Optional<User> findByEduIDS_serviceProviderEntityId(String serviceProviderEntityId);

    List<User> findByNewUserTrueAndCreatedLessThan(long millis);

    Optional<User> findUserByWebAuthnIdentifier(String webAuthnIdentifier);

    Optional<User> findUserByUserHandle(String userHandle);

    Optional<User> findUserByEnrollmentVerificationKey(String enrollmentVerificationKey);

    Optional<User> findUserByCreateFromInstitutionKey(String createFromInstitutionKey);

}
