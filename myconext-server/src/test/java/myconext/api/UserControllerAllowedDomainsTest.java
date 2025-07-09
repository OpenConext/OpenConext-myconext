package myconext.api;

import myconext.AbstractIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

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
                "feature.use_deny_allow_list.allow_enabled=True",
                "feature.captcha_enabled=false"
        })
@ActiveProfiles({"test"})
public class UserControllerAllowedDomainsTest extends AbstractIntegrationTest {

    @Test
    public void allowedEmailNewUser() throws IOException {
        oneTimeLoginCodeRequest(user("jdoe@SUBDOMAIN.stRanGe.ME"), HttpMethod.POST)
                .response
                .statusCode(HttpStatus.CREATED.value());
    }

    @Test
    public void notAllowedEmailNewUser() throws IOException {
        oneTimeLoginCodeRequest(user("jdoe@example.com"), HttpMethod.POST)
                .response
                .statusCode(HttpStatus.FORBIDDEN.value());
    }

}
