package myconext.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class ResourceGoneException extends RuntimeException{

    public ResourceGoneException(String message) {
        super(message);
    }
}
