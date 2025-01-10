package myconext.api;

import myconext.AbstractIntegrationTest;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvironmentControllerTest extends AbstractIntegrationTest {

    @Test
    void disclaimer() throws IOException {
        InputStream inputStream = given()
                .when()
                .accept("text/css")
                .get("/myconext/api/disclaimer")
                .asInputStream();
        String css = IOUtils.toString(inputStream, Charset.defaultCharset());
        assertEquals(css, "body::after {background: red;content: \"LOCAL\";}");
    }

}