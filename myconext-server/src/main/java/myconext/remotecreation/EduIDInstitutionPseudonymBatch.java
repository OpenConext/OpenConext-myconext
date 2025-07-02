package myconext.remotecreation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class EduIDInstitutionPseudonymBatch {

    @NotBlank
    @Schema(description = "Must be pre-provisioned in Manage for a valid IdP")
    private String brinCode;

    @NotEmpty
    @Schema(description = "Previous assigned eduID values scoped on external remote API user")
    private List<String> eduIDValues;
}
