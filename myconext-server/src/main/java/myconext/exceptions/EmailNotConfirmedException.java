package myconext.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PRECONDITION_FAILED)
public class EmailNotConfirmedException extends RuntimeException {

    private String uid;

    public EmailNotConfirmedException(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }
}
