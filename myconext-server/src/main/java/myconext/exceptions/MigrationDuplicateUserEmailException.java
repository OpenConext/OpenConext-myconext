package myconext.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
@Getter
public class MigrationDuplicateUserEmailException extends RuntimeException {

    private final String email;

    public MigrationDuplicateUserEmailException(String email) {
        this.email = email;
    }
}
