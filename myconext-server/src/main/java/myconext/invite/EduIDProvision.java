package myconext.invite;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@ToString
public class EduIDProvision {

    @NotNull
    private String eduIDValue;
    @NotNull
    private String institutionGUID;

}
