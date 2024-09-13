package myconext.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class DisposableEmailProviderException extends AbstractBaseException {

    public DisposableEmailProviderException(String message) {
        super(message);
    }
}
