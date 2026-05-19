package myconext.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdateUserSecurityRequest implements Serializable {

    @NotBlank
    private String newPassword;
    @NotBlank
    private String hash;
}
