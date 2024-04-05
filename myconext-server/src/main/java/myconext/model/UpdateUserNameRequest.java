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

    private String chosenName;

    @NotBlank
    private String givenName;

    @NotBlank
    private String familyName;
}
