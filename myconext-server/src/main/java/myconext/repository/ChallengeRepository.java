package myconext.repository;


import myconext.model.Challenge;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChallengeRepository extends MongoRepository<Challenge, String> {

    Optional<Challenge> findByToken(String token);
}
