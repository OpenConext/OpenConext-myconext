package myconext.remotecreation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import myconext.model.Verification;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.ValidationException;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class NewExternalEduID implements Serializable {

    private String email;

    @NotBlank
    private String chosenName;

    private String firstName;

    private String lastNamePrefix;

    @NotBlank
    private String lastName;

    @Schema(description = "Date of birth in the format 'yyyyMMdd' or 'yyyy-MM-dd'")
    private String dateOfBirth;

    @NotBlank
    private String identifier;

    @NotNull
    private Verification verification;

    @Schema(description = "Can be initial NULL")
    private List<String> brinCodes;

    public void validate() {
        List<String> requiredAttributes = new ArrayList<>();
        if (!verification.equals(Verification.Ongeverifieerd) && !StringUtils.hasText(dateOfBirth)) {
            requiredAttributes.add("dateOfBirth");
        }
        if (!verification.equals(Verification.Ongeverifieerd) && !StringUtils.hasText(firstName)) {
            requiredAttributes.add("firstName");
        }
        if (!CollectionUtils.isEmpty(requiredAttributes)) {
            throw new ValidationException(String.format("Required attributes '%s' missing for verification %s",
                    String.join(", ", requiredAttributes), verification));
        }
    }

}
