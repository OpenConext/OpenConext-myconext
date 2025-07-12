package myconext.invite;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class EduIDProvision {

    @NotNull
    private String eduIDValue;
    @NotNull
    private String institutionGUID;

}
