package myconext.model;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VerifyOneTimeLoginCode implements Serializable {

    @NotBlank
    private String code;

    private String authenticationRequestId;

    private String hash;

    public VerifyOneTimeLoginCode(String code) {
        this(code, null);
    }

    public VerifyOneTimeLoginCode(String code, String authenticationRequestId) {
        this(code, authenticationRequestId, null);
    }
}
