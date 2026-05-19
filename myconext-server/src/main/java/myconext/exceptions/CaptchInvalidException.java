package myconext.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_REQUIRED)
public class CaptchInvalidException extends AbstractBaseException {

    public CaptchInvalidException(String message) {
        super(message);
    }
}
