package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteService implements Serializable {

    @NotNull
    private String serviceProviderEntityId;
    private List<TokenRepresentation> tokens;

}
