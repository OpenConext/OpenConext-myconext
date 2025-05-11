package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
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

}
