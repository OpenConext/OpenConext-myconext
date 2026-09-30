package myconext.api;

import io.restassured.RestAssured;
import myconext.MyConextServerApplication;
import myconext.model.ExternalLinkedAccount;
import myconext.model.IdpScoping;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static myconext.AbstractIntegrationTest.user;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricsControllerTest {

    @Test
    void prometheus() throws IOException {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(MyConextServerApplication.class,
                "--server.port=8098", "--server.shutdown=immediate");
        RestAssured.port = 8098;

        //BRIN codes are data-driven, so seed one to deterministically assert the new per-BRIN metric
        UserRepository userRepository = applicationContext.getBean(UserRepository.class);
        User brinUser = user("brin-metrics-test@example.com", "en");
        ExternalLinkedAccount externalLinkedAccount = new ExternalLinkedAccount("subjectID", IdpScoping.studielink, true);
        externalLinkedAccount.setBrinCodes(List.of("ST42"));
        brinUser.getExternalLinkedAccounts().add(externalLinkedAccount);
        userRepository.save(brinUser);

        MetricsController metricsController = applicationContext.getBean(MetricsController.class);
        metricsController.refreshExternalLinkedAccountsByBrin();

        InputStream inputStream = given()
                .when()
                .auth().basic("internal", "secret")
                .get("/internal/prometheus")
                .asInputStream();

        String metrics = IOUtils.toString(inputStream, Charset.defaultCharset());

        userRepository.delete(brinUser);
        SpringApplication.exit(applicationContext);

        List.of("user_count", "linked_account_count", "app_registration_count", "used_services_count", "total_external_linked_account_count")
                .forEach(s -> assertTrue(metrics.contains(s)));
        Stream.of(IdpScoping.values()).forEach(idpScoping ->
                assertTrue(metrics.contains("external_linked_account_" + idpScoping.name())));

        assertTrue(metrics.contains("external_linked_account_count_by_brin"));
        assertTrue(metrics.contains("brin=\"ST42\""));
    }

}
