package myconext.oidcng;

import myconext.model.Token;
import myconext.model.TokenRepresentation;
import myconext.model.User;
import org.springframework.http.HttpStatusCode;

import java.util.List;

public interface OpenIDConnect {

    List<Token> tokens(User user);

    HttpStatusCode deleteTokens(List<TokenRepresentation> tokenIdentifiers, User user);
}
