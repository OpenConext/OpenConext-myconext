package myconext.repository;


import myconext.model.User;
import myconext.model.UserLogin;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserLoginRepository extends MongoRepository<UserLogin, String> {

}
