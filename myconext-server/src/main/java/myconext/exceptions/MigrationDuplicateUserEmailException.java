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
        this.email = email;
        this.requestUrl = requestUrl;
    }

    @Override
    public String toString() {
        return String.format("%s with email: %s during request: %s", this.getClass().getSimpleName(), email, requestUrl);
    }
}
