package myconext.oidcng;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.DeleteService;
import myconext.model.DeleteServiceTokens;
import myconext.model.TokenRepresentation;
import myconext.model.TokenType;
import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "mongodb_db=surf_id_test",
                "cron.node-cron-job-responsible=false",
                "email_guessing_sleep_millis=1",
                "sp_entity_id=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "sp_entity_metadata_url=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "spring.main.lazy-initialization=true",
                "oidc-token-api.token-url=http://localhost:8098/tokens",
                "host_headers.active: mijn.test2.eduid.nl",
                "oidc-token-api.enabled=True"
        })
public class OpenIDConnectTest extends AbstractIntegrationTest {

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8098);

    @Test
    @SuppressWarnings("unchecked")
    public void tokens() throws IOException {
        stubForTokens("oidcng/tokens.json", 200);
        List<Map<String, Object>> res = given()
                .when()
                .accept(ContentType.JSON)
                .get("/myconext/api/sp/tokens")
                .getBody().as(List.class);
        assertEquals(4, res.size());

        res.forEach(l -> assertTrue(l.containsKey("clientId")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void tokens400() throws IOException {
        stubForTokens("oidcng/error.json", 500);
        given()
                .when()
                .accept(ContentType.JSON)
                .get("/myconext/api/sp/tokens")
                .then()
                .statusCode(200)
                .body("", Matchers.hasSize(0));
    }

    @Test
    public void deleteTokens() {
        stubFor(put(urlPathMatching("/tokens"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.NO_CONTENT.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody("{}")));
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new DeleteService("http://mock-sp", Arrays.asList(new TokenRepresentation("id", TokenType.ACCESS))))
                .put("/myconext/api/sp/service")
                .then()
                .statusCode(200);
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(new DeleteServiceTokens(Arrays.asList(new TokenRepresentation("id", TokenType.ACCESS))))
                .put("/myconext/api/sp/tokens")
                .then()
                .statusCode(200);

    }

    private void stubForTokens(String jsonPath, int status) throws IOException {
        stubFor(get(urlPathMatching("/tokens"))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFile(jsonPath))));
    }

}