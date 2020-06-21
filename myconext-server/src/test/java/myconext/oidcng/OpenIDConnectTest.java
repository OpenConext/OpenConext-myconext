package myconext.oidcng;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
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
                "oidc-token-api.token-url=http://localhost:8099/tokens",
        })
public class OpenIDConnectTest extends AbstractIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8099);

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
                .statusCode(400);
    }

    private void stubForTokens(String jsonPath, int status) throws IOException {
        stubFor(get(urlPathMatching("/tokens"))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFile(jsonPath))));
    }

}