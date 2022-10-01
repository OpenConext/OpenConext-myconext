package myconext.exceptions;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
@NoArgsConstructor
public class DisposableEmailProviderException extends RuntimeException {
}
