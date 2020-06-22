package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteServiceTokens {

    private String eduId;
    private List<TokenRepresentation> tokens;

}
