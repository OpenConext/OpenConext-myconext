package myconext.activity;

import myconext.model.ActivityEvent;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Generic helper to store the actions of a user in the user document, see {@link User#getActivities()}.
 */
@Component
public class ActivityRecorder {

    private final UserRepository userRepository;

    public ActivityRecorder(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Add the activity to the user and persist the user. A new user is persisted first, as the activity is
     * stored with the id of the user.
     *
     * @return the persisted user
     */
    public User record(User user, ActivityEvent event, Map<String, String> context) {
        User persisted = user.getId() == null ? userRepository.save(user) : user;
        persisted.addActivity(event, context);
        return userRepository.save(persisted);
    }
}
