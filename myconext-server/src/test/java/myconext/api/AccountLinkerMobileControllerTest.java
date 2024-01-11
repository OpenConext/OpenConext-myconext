package myconext.api;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import myconext.AbstractIntegrationTest;
import myconext.model.User;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccountLinkerMobileControllerTest extends AbstractIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8098);

    @Test
    @SuppressWarnings("unchecked")
    public void spOidcLinkMobile() throws IOException {
        Map<String, Object> results = given()
                .when()
                .contentType(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .get("/mobile/api/sp/oidc/link")
                .as(Map.class);

        assertTrue(((String) results.get("url")).startsWith("http://localhost:8098/oidc/authorize?" +
                "scope=openid&" +
                "response_type=code&" +
                "redirect_uri=http://localhost:8081/myconext/api/mobile/oidc/redirect"));

        String hash = mobileLinkAccountRequestRepository.findAll().get(0).getHash();

        Map<Object, Object> userInfo = new HashMap<>();
        userInfo.put("eduperson_principal_name", "eppn@ex.com");
        userInfo.put("schac_home_organization", "mock.idp");

        stubForTokenUserInfo(userInfo);
        String location = given().redirects().follow(false)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", hash)
                .contentType(ContentType.JSON)
                .get("/myconext/api/mobile/oidc/redirect")
                .getHeader("Location");
        assertEquals("http://localhost:3000/client/mobile/account-linked?institution=eppn%40ex.com", location);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void spOidcLinkMobileEppnAlreadyExists() throws IOException {
        //Need to call this, otherwise no MobileLinkAccountRequest is created
        given()
                .when()
                .contentType(ContentType.JSON)
                .auth().oauth2(opaqueAccessToken(true, "eduid.nl/mobile"))
                .get("/mobile/api/sp/oidc/link")
                .as(Map.class);

        String hash = mobileLinkAccountRequestRepository.findAll().get(0).getHash();

        Map<Object, Object> userInfo = new HashMap<>();
        userInfo.put("eduperson_principal_name", "1234567890@surfguest.nl");
        userInfo.put("schac_home_organization", "mock.idp");

        stubForTokenUserInfo(userInfo);
        String location = given().redirects().follow(false)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", hash)
                .contentType(ContentType.JSON)
                .get("/myconext/api/mobile/oidc/redirect")
                .getHeader("Location");

        assertEquals(0, mobileLinkAccountRequestRepository.count());
        assertEquals(location, "http://localhost:3000/client/mobile/eppn-already-linked?email=jdoe%40example.com");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void spOidcLinkMobileExpired() {
        String location = given().redirects().follow(false)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", "nope")
                .contentType(ContentType.JSON)
                .get("/myconext/api/mobile/oidc/redirect")
                .getHeader("Location");
        assertEquals(location, "http://localhost:3000/client/mobile/expired");
    }

}