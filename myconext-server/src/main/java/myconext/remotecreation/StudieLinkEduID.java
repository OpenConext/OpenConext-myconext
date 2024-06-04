package myconext.remotecreation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import myconext.model.Verification;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StudieLinkEduID implements Serializable {

    private String email;
    private String eduIDValue;
    private String chosenName;
    private String firstName;
    private String lastNamePrefix;
    private String lastName;
    @Schema(description = "Date of birth in the format 'yyyyMMdd' or 'yyyy-MM-dd'")
    private String dateOfBirth;
    private String identifier;
    private Verification verification;
    private String brinCode;

}
