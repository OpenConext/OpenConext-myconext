package myconext.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
public class CreateInstitutionEduID implements Serializable {

    @NotNull
    private String hash;

    @NotNull
    private String email;

    @NotNull
    private String givenName;

    @NotNull
    private String familyName;
}
