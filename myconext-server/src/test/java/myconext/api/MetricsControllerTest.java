package myconext.api;

import io.restassured.RestAssured;
import myconext.MyConextServerApplication;
import myconext.model.IdpScoping;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetricsControllerTest {

    @Test
    void prometheus() throws IOException {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(MyConextServerApplication.class, "--server.port=8098");
        RestAssured.port = 8098;

        InputStream inputStream = given()
                .when()
                .auth().basic("internal", "secret")
                .get("/internal/prometheus")
                .asInputStream();
        SpringApplication.exit(applicationContext);

        String metrics = IOUtils.toString(inputStream, Charset.defaultCharset());

        List.of("user_count","linked_account_count","registered_apps_count")
                .forEach(s -> assertTrue(metrics.contains(s)));
        Stream.of(IdpScoping.values()).forEach(idpScoping ->
                assertTrue(metrics.contains("external_linked_account_" + idpScoping.name())));

    }

}