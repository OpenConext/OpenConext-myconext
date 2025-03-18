package myconext.log;

import jakarta.servlet.http.HttpServletRequest;
import myconext.model.User;
import org.apache.commons.logging.Log;
import org.slf4j.MDC;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

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

    /**
     * "type":"login" (for filtering)
     * date and time
     * remote ip address
     * username
     * login methode
     * useragent
     * status (start, ingelogd, PB verstuurd, gefaald, enz.)
     */
    public static void logLoginWithContext(User user, String loginMethod, boolean success, Log log, String message,
                                           HttpServletRequest request) {
        MDC.setContextMap(Map.of(
                "login_method", loginMethod,
                "action", "login",
                "result", success ? "ok" : "error",
                "tag", "myconext_loginstats",
                "userid", user.getEmail()));
        log.info(String.format("%S %s %s", message, user.getEmail(), user.getId()));
    }

    /*
     * Resolves client IP address when application is behind a NGINX or other reverse proxy server
     */
    public static String resolve(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For"); // used by the majority of load balancers
        String xRealIp = request.getHeader("X-Real-IP"); // used by Nginx
        String remoteAddr = request.getRemoteAddr(); // otherwise uses the remote IP address obtained by our Servlet container
        // returns the first non-null
        return Stream.of(xRealIp, xForwardedFor, remoteAddr)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }
}
