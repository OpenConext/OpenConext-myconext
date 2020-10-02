package myconext.log;

import myconext.model.User;
import org.apache.commons.logging.Log;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.springframework.util.Assert;

public class MDCContext {

    public static final String USER_ID = "userid";

    private static void mdcContext(User user, String... args) {
        Assert.isTrue(args.length % 2 == 0, "contextMap requires an even number of arguments");
        MDCAdapter mdcAdapter = MDC.getMDCAdapter();
        for (int i = 0; i < args.length - 1; i += 2) {
            mdcAdapter.put(args[i], args[i + 1]);
        }

        mdcAdapter.put("tag", "myconext_loginstats");

        if (MDC.get(USER_ID) == null && user != null) {
            mdcAdapter.put(USER_ID, user.getEmail());
        }
    }

    public static void logWithContext(User user, String action, String target, Log log, String message) {
        mdcContext(user, "action", action, "target", target, "resullt", "ok");
        log.info(message);
    }


    public static void logLoginWithContext(User user, String loginMethod, boolean success, Log log, String message) {
        mdcContext(user, "login_method", loginMethod, "action", "login", "result", success ? "ok" : "error");
        log.info(message);
    }

}
