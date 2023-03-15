package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Token implements Serializable {

    private String id;
    private String expiresIn;
    private String createdAt;
    private String clientName;
    private String clientId;
    private TokenType type;

    private List<String> audiences;
    private List<Scope> scopes;
}
