package myconext.api;


import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletRequest;
import myconext.exceptions.RemoteException;
import myconext.exceptions.UserNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.net.URISyntaxException;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestController
@Hidden
public class DefaultErrorController implements ErrorController {

    private static final Log LOG = LogFactory.getLog(DefaultErrorController.class);

    private final ErrorAttributes errorAttributes;

    @Autowired
    public DefaultErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity error(HttpServletRequest request) throws URISyntaxException {
        WebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> result = this.errorAttributes.getErrorAttributes(
                webRequest,
                ErrorAttributeOptions.of(
                        ErrorAttributeOptions.Include.EXCEPTION,
                        ErrorAttributeOptions.Include.STATUS,
                        ErrorAttributeOptions.Include.MESSAGE,
                        ErrorAttributeOptions.Include.ERROR,
                        ErrorAttributeOptions.Include.BINDING_ERRORS)
        );

        Throwable error = this.errorAttributes.getError(webRequest);
        HttpStatus statusCode;

        if (error == null) {
            if ("unauthorized".equalsIgnoreCase((String) result.getOrDefault("message", ""))) {
                statusCode = UNAUTHORIZED;
            } else {
                statusCode = result.containsKey("status") && (int) result.get("status") != 999 ?
                        HttpStatus.valueOf((int) result.get("status")) : INTERNAL_SERVER_ERROR;
            }

        } else {
//            if (error instanceof UserNotFoundException) {
//                LOG.warn(String.format("%s: %s", error.getClass(), error.getMessage()));
//            } else {
                LOG.error(String.format("Error occurred; %s %s", error.getClass(), error), error);
//            }
            //https://github.com/spring-projects/spring-boot/issues/3057
            ResponseStatus annotation = AnnotationUtils.getAnnotation(error.getClass(), ResponseStatus.class);
            if (annotation != null) {
                statusCode = annotation.value();
            } else if (result.containsKey("status") && !result.get("status").equals(500)) {
                statusCode = HttpStatus.valueOf((Integer) result.get("status"));
            } else {
                statusCode = BAD_REQUEST;
            }
            if (error instanceof RemoteException) {
                result.put("reference", ((RemoteException)error).getReference());
            }
        }
        result.put("status", statusCode.value());
        return ResponseEntity.status(statusCode).body(result);
    }

}
