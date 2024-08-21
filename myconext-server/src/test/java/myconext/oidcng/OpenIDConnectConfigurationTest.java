package myconext.oidcng;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.model.Token;
import myconext.model.TokenRepresentation;
import myconext.model.TokenType;
import myconext.model.User;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OpenIDConnectConfigurationTest {

    @Test
    public void openIDConnectMock() throws IOException {
        OpenIDConnect openIDConnect = new OpenIDConnectConfiguration().openIDConnectMock(new ObjectMapper());
        List<Token> tokens = openIDConnect.tokens(new User());

        assertEquals(4, tokens.size());
        assertEquals(HttpStatus.NO_CONTENT, openIDConnect.deleteTokens(Collections.emptyList(), new User()));

        openIDConnect.deleteTokens(List.of(new TokenRepresentation(tokens.get(0).getId(), TokenType.ACCESS)), new User());

        tokens = openIDConnect.tokens(new User());
        assertEquals(3, tokens.size());
    }
}