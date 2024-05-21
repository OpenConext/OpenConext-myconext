package myconext.api;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import myconext.AbstractIntegrationTest;
import myconext.model.VerifyIssuer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;


class VerifyControllerTest extends AbstractIntegrationTest {

    @Test
    void issuers() {
        List<VerifyIssuer> issuers = given()
                .when()
                .contentType(ContentType.JSON)
                .get("/myconext/api/sp/idin/issuers")
                .as(new TypeRef<>() {
                });
        assertEquals(7, issuers.size());
        assertEquals(List.of(
                "ABNANL2A",
                "ASNBNL21",
                "BUNQNL2A",
                "INGBNL2A",
                "RABONL2U",
                "RBRBNL21",
                "SNSBNL2A"
        ), issuers.stream().map(VerifyIssuer::getId).sorted().collect(Collectors.toList()));
    }
}