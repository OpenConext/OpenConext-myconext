package myconext.oidcng;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.model.TokenRepresentation;
import myconext.model.TokenType;
import myconext.model.User;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class OpenIDConnectConfigurationTest {

    @Test
    public void openIDConnectMock() throws IOException {
        OpenIDConnect openIDConnect = new OpenIDConnectConfiguration().openIDConnectMock(new ObjectMapper());
        List<Map<String, Object>> tokens = openIDConnect.tokens(new User());

        assertEquals(4, tokens.size());
        assertEquals(HttpStatus.NO_CONTENT, openIDConnect.deleteTokens(Collections.emptyList(), new User()));
        assertEquals(4, tokens.size());

        openIDConnect.deleteTokens(Arrays.asList(new TokenRepresentation((String) tokens.get(0).get("id"), TokenType.ACCESS)), new User());

        tokens = openIDConnect.tokens(new User());
        assertEquals(3, tokens.size());
    }
}