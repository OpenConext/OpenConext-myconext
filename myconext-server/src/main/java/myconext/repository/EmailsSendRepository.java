package myconext.repository;


import myconext.model.ChangeEmailHash;
import myconext.model.EmailsSend;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailsSendRepository extends MongoRepository<EmailsSend, String> {

    @Query(collation = "{ 'locale' : 'en_US', 'strength' : 2 }")
    Optional<EmailsSend> findByEmail(String email);

    Long deleteBySendAtBefore(Date expiryDate);

}
