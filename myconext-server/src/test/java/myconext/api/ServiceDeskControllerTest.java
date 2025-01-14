package myconext.api;

import io.restassured.filter.Filter;
import io.restassured.filter.cookie.CookieFilter;
import myconext.AbstractIntegrationTest;
import myconext.model.ControlCode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "service_desk_role_auto_provisioning=True"
        })
class ServiceDeskControllerTest extends AbstractIntegrationTest {

}