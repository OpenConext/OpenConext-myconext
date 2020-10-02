package myconext.oidcng;

import myconext.model.TokenRepresentation;
import myconext.model.User;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

public interface OpenIDConnect {

    List<Map<String, Object>> tokens(User user);

    HttpStatus deleteTokens(List<TokenRepresentation> tokenIdentifiers, User user);
}
