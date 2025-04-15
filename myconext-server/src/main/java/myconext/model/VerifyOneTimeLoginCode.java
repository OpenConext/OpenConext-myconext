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
    @NotBlank
    private String authenticationRequestId;
}
