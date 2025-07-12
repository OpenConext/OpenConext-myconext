package myconext.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;


@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateAccount implements Serializable {

    @NotNull
    private String email;
    @NotNull
    private String givenName;
    @NotNull
    private String familyName;
    @NotNull
    private String relyingPartClientId;

}
