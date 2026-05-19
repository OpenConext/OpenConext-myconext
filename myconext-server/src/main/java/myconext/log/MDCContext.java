package myconext.log;

import jakarta.servlet.http.HttpServletRequest;
import myconext.model.User;
import org.apache.commons.logging.Log;
import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class MDCContext {

    private MDCContext() {
    }

    public static void logWithContext(User user, String action, String target, Log log, String message) {
        MDC.setContextMap(Map.of(
                "action", action,
                "target", target,
                "result", "ok",
                "tag", "myconext_loginstats",
                "userid", user.getEmail()));
        log.info(String.format("%s %s %s", message, user.getEmail(), user.getId()));
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
                                           HttpServletRequest request, String authnContextClassRefValue,
                                           List<String> authenticationContextClassReferences) {
        MDC.setContextMap(Map.of(
                "login_method", loginMethod,
                "action", "login",
                "result", success ? "ok" : "error",
                "tag", "myconext_loginstats",
                "userid", user.getEmail()));
        String ipAddress = resolve(request);
        String userAgent = request.getHeader("User-Agent");
        log.info(String.format("%s %s %s, ipAddress: %s, type: %s, userAgent: %s, requestedACR: %s, responseACR: %s",
                message,
                user.getEmail(),
                user.getId(),
                ipAddress,
                loginMethod,
                userAgent,
                CollectionUtils.isEmpty(authenticationContextClassReferences) ? "[]" :
                        String.join(", ", authenticationContextClassReferences),
                authnContextClassRefValue
        ));
    }

    /*
     * Resolves client IP address when application is behind a NGINX or other reverse proxy server
     */
    public static String resolve(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String xRealIp = request.getHeader("X-Real-IP");
        String remoteAddr = request.getRemoteAddr();
        return Stream.of(xRealIp, xForwardedFor, remoteAddr)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }
}
