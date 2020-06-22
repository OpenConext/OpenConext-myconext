package myconext.oidcng;

import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.model.User;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

public class OpenIDConnectConfigurationTest {

    @Test
    public void openIDConnectMock() throws IOException {
        OpenIDConnect openIDConnect = new OpenIDConnectConfiguration().openIDConnectMock(new ObjectMapper());
        assertEquals(4, openIDConnect.tokens(new User()).size());
        assertEquals(HttpStatus.NO_CONTENT, openIDConnect.deleteTokens(Collections.emptyList()));
    }
}