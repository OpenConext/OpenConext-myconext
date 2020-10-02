package myconext.oidcng;

import myconext.model.TokenRepresentation;
import myconext.model.User;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OpenIDConnectMock implements OpenIDConnect {

    private List<Map<String, Object>> tokens;

    public OpenIDConnectMock(List<Map<String, Object>> tokens) {
        this.tokens = tokens;
    }

    @Override
    public List<Map<String, Object>> tokens(User user) {
        return tokens;
    }

    @Override
    public HttpStatus deleteTokens(List<TokenRepresentation> tokenIdentifiers, User user) {
        this.tokens = this.tokens.stream()
                .filter(token -> tokenIdentifiers.stream()
                        .noneMatch(tokenRepresentation -> tokenRepresentation.getId().equals(token.get("id"))))
                .collect(Collectors.toList());
        return HttpStatus.NO_CONTENT;
    }
}
