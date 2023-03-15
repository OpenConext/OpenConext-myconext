package myconext.oidcng;

import myconext.model.Token;
import myconext.model.TokenRepresentation;
import myconext.model.User;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OpenIDConnectMock implements OpenIDConnect {

    private List<Token> tokens;

    public OpenIDConnectMock(List<Token> tokens) {
        this.tokens = tokens;
    }

    @Override
    public List<Token> tokens(User user) {
        return tokens;
    }

    @Override
    public HttpStatus deleteTokens(List<TokenRepresentation> tokenIdentifiers, User user) {
        this.tokens = this.tokens.stream()
                .filter(token -> tokenIdentifiers.stream()
                        .noneMatch(tokenRepresentation -> tokenRepresentation.getId().equals(token.getId())))
                .collect(Collectors.toList());
        return HttpStatus.NO_CONTENT;
    }
}
