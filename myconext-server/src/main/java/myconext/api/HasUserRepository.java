package myconext.api;

import myconext.model.User;
import myconext.repository.UserRepository;
import org.springframework.util.StringUtils;

import java.util.Optional;

public interface HasUserRepository {

    UserRepository getUserRepository();

    default Optional<User> findUserByEduIDValue(String eduIDValue) {
        return StringUtils.hasText(eduIDValue) ? getUserRepository().findByEduIDS_value(eduIDValue) : Optional.empty();
    }

}
