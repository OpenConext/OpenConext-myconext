package myconext.remotecreation;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Must be pre-provisioned in Manage for a valid IdP")
    private String brinCode;
    @NotNull
    @Schema(description = "Previous assigned eduID scoped on external remote API user")
    private String eduID;
}
