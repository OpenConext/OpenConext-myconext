package myconext.remotecreation;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EduIDAssignedValue {

    @Schema(description = "Previous assigned eduID scoped on external remote API user")
    private String eduID;

    @Schema(description = "New assigned eduID pseudonym scoped on external remote API user")
    private String value;

    @Schema(description = "The brinCode of the institution for the new assigned eduID pseudonym ")
    private String brinCode;
}
