package myconext.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class WeakPasswordException extends AbstractBaseException {

    public WeakPasswordException(String message) {
        super(message);
    }
}
