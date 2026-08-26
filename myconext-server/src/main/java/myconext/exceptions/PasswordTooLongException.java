package myconext.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
public class PasswordTooLongException extends AbstractBaseException {

    public PasswordTooLongException(String message) {
        super(message);
    }
}
