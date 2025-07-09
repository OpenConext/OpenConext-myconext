package myconext.api;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import myconext.AbstractIntegrationTest;
import myconext.model.ClientAuthenticationRequest;
import myconext.model.User;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "mongodb_db=surf_id_test",
                "cron.node-cron-job-responsible=false",
                "email_guessing_sleep_millis=1",
                "sp_entity_id=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "sp_entity_metadata_url=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "spring.main.lazy-initialization=true",
                "cron.service-name-resolver-initial-delay-milliseconds=60000",
                "feature.use_deny_allow_list.allow_enabled=false",
                "feature.captcha_enabled=true"
        })
@ActiveProfiles({"test"})
public class UserControllerCaptchaTest extends AbstractIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8098);

    @Test
    public void postNewUserCaptchaCheck() throws IOException {
        //Stub for captcha validation call
        stubFor(post(urlPathMatching("/api/v2/captcha/siteverify")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(Map.of("success", true)))));

        User user = user("new@example.com", "Mary", "Doe", "en");

        ClientAuthenticationResponse authenticationResponse = oneTimeLoginCodeRequest(user, HttpMethod.POST);
        String samlResponse = samlResponse(authenticationResponse);
        assertTrue(samlResponse.contains("new@example.com"));
    }

    @Test
    public void postNewUserCaptchaCheckFailure() throws IOException {
        //Stub for captcha validation call
        stubFor(post(urlPathMatching("/api/v2/captcha/siteverify")).willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(objectMapper.writeValueAsString(Map.of("success", false)))));

        User user = user("new@example.com", "Mary", "Doe", "en");
        String authenticationRequestId = samlAuthnRequest();
        ClientAuthenticationRequest clientAuthenticationRequest = new ClientAuthenticationRequest(authenticationRequestId, user, StringUtils.hasText(user.getPassword()), "response");
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(clientAuthenticationRequest)
                .post("/myconext/api/idp/generate_code_request")
                .then()
                .statusCode(HttpStatus.PRECONDITION_REQUIRED.value());

    }
}
