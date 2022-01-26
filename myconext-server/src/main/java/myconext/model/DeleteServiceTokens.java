package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteServiceTokens implements Serializable {

    private String eduId;
    private List<TokenRepresentation> tokens;

}
