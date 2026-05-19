package myconext.tiqr;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
