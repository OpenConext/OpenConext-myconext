package myconext.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
@Getter
public class MigrationDuplicateUserEmailException extends RuntimeException {

    private String email;
    private String requestUrl;

    public MigrationDuplicateUserEmailException(String email, String requestUrl) {
        super(String.format("MigrationDuplicateUserEmailException with email: %s during request: %s", email, requestUrl));
        this.email = email;
        this.requestUrl = requestUrl;
    }

}
