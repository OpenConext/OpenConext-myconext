package myconext.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ExpiredAuthenticationException extends AbstractBaseException {

    public ExpiredAuthenticationException(String message) {
        super(message);
    }

    @Override
    protected boolean suppressStackTrace() {
        return true;
    }
}
