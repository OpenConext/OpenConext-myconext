package myconext.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.Issuer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;


class VerifyControllerTest extends AbstractIntegrationTest {

    @Test
    void issuers() {
        List<Issuer> issuers = given()
                .when()
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/idin/issuers")
                .as(new TypeRef<>() {
                });
        assertEquals(1, issuers.size());
        assertEquals(List.of("RABONL2U"), issuers.stream().map(Issuer::getId).sorted().collect(Collectors.toList()));
    }
}