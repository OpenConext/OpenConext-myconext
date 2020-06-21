package myconext.oidcng;

import myconext.model.TokenRepresentation;
import myconext.model.User;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public class OpenIDConnectMock implements OpenIDConnect {


    private final List<Map<String, Object>> tokens;

    public OpenIDConnectMock(List<Map<String, Object>> tokens) {
        this.tokens = tokens;
    }

    @Override
    public List<Map<String, Object>> tokens(User user) {
        return tokens;
    }

    @Override
    public HttpStatus deleteTokens(List<TokenRepresentation> tokenIdentifiers) {
        return HttpStatus.NO_CONTENT;
    }
}
