package myconext.log;

import myconext.model.User;
import org.slf4j.MDC;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MDCContext {

    public static void mdcContext(String... args) {
        mdcContext(Optional.empty(), args);
    }

    public static void mdcContext(Optional<User> optionalUser, String... args) {
        Assert.isTrue(args.length % 2 == 0, "contextMap requires an even number of arguments");
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length - 1; i+=2) {
            map.put(args[i], args[i + 1]);
        }
        if (MDC.get("user_id") == null && optionalUser.isPresent()) {
            User user = optionalUser.get();
            map.put("user_id", user.getId());
            map.put("user_email", user.getEmail());
        };
        MDC.setContextMap(map);
    }


}
