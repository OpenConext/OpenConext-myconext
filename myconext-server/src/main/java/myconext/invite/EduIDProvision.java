package myconext.invite;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;

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
