package myconext.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends AbstractBaseException {

    public UserNotFoundException(String message) {
        super(message);
    }

    @Override
    protected boolean suppressStackTrace() {
        return false;
    }
}
