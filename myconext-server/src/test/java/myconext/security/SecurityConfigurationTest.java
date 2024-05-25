package myconext.security;


import myconext.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.given;
import static org.hamcrest.core.IsEqual.equalTo;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "mongodb_db=surf_id_test",
                "private_key_path=classpath:/nope",
                "certificate_path=classpath:/nope.pub",
                "cron.node-cron-job-responsible=false",
                "sp_entity_id=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "sp_entity_metadata_url=https://engine.test.surfconext.nl/authentication/sp/metadata",
                "management.health.mail.enabled=false"
        })
@ActiveProfiles("dev")
public class SecurityConfigurationTest extends AbstractIntegrationTest {

    @Test
    public void health() {
        given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .get("internal/health")
                .then()
                .body("status", equalTo("UP"));

    }
}
