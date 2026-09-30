package myconext.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ServiceProviderNotFoundException extends AbstractBaseException {

    public ServiceProviderNotFoundException(String message) {
        super(message);
    }

}
