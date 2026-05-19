package myconext.tiqr;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DeactivateRequest implements Serializable {

    @NotNull(message = "'verificationCode' is required")
    private String verificationCode;

}
