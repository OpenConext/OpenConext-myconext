package myconext.tiqr;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TiqrRequest implements Serializable {

    @NotNull
    private String authenticationRequestId;

    @NotNull
    private String email;

}
