package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateUserNameRequest implements Serializable {

    @NotBlank
    private String callName;

    @NotBlank
    private String givenName;

    @NotBlank
    private String familyName;
}
