package myconext.remotecreation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class EduIDInstitutionPseudonym {
    @NotNull
    private String brinCode;
    @NotNull
    private String eduID;
}
