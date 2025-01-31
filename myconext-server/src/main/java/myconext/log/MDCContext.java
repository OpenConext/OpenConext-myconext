package myconext.log;

import myconext.model.User;
import org.apache.commons.logging.Log;
import org.slf4j.MDC;

import java.util.Map;

public class MDCContext {

    public static void logWithContext(User user, String action, String target, Log log, String message) {
        MDC.setContextMap(Map.of(
                "action", action,
                "target", target,
                "result", "ok",
                "tag", "myconext_loginstats",
                "userid", user.getEmail()));
        log.info(String.format("%S %s %s", message, user.getEmail(), user.getId()));
    }


    public static void logLoginWithContext(User user, String loginMethod, boolean success, Log log, String message) {
        MDC.setContextMap(Map.of(
                "login_method", loginMethod,
                "action", "login",
                "result", success ? "ok" : "error",
                "tag", "myconext_loginstats",
                "userid", user.getEmail()));
        log.info(String.format("%S %s %s", message, user.getEmail(), user.getId()));
    }

}
