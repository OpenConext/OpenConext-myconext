package myconext.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class CreateInstitutionEduID implements Serializable {

    @NotNull
    private String hash;

    @NotNull
    private String email;

    private boolean isNewUser;

}
