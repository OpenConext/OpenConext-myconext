package myconext.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ClientAuthenticationRequest implements Serializable {

    @NotNull
    private String authenticationRequestId;

    @NotNull
    private User user;

    private boolean usePassword;

    private String captchaResponse;

}
