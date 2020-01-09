package myconext.api;

import myconext.exceptions.MigrationDuplicateUserEmailException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
public class DefaultErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;
    private final String redirectUrl;

    @Autowired
    public DefaultErrorController(ErrorAttributes errorAttributes, @Value("${sp_redirect_url}") String redirectUrl) {
        this.errorAttributes = errorAttributes;
        this.redirectUrl = redirectUrl;
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/error")
    public ResponseEntity error(HttpServletRequest request) throws URISyntaxException {
        WebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> result = this.errorAttributes.getErrorAttributes(webRequest, false);

        Throwable error = this.errorAttributes.getError(webRequest);
        HttpStatus statusCode;

        if (error == null) {
            statusCode = result.containsKey("status") && (int) result.get("status") != 999 ?
                    HttpStatus.valueOf((int) result.get("status")) : INTERNAL_SERVER_ERROR;
        } else {
            //https://github.com/spring-projects/spring-boot/issues/3057
            ResponseStatus annotation = AnnotationUtils.getAnnotation(error.getClass(), ResponseStatus.class);
            statusCode = annotation != null ? annotation.value() : BAD_REQUEST;
            if (error instanceof MigrationDuplicateUserEmailException) {
                String email = ((MigrationDuplicateUserEmailException) error).getEmail();
                return ResponseEntity.status(302).location(new URI(this.redirectUrl + "/migration-error?email=" + email)).build();
            }

        }

        result.remove("message");
        result.put("status", statusCode.value());

        return ResponseEntity.status(statusCode).body(result);
    }

}
