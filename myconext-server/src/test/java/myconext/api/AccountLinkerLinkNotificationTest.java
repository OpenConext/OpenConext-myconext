package myconext.api;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.http.ContentType;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import myconext.AbstractMailBoxTest;
import myconext.model.ClientAuthenticationRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AccountLinkerLinkNotificationTest extends AbstractMailBoxTest {

    @Autowired
    private AccountLinkerController accountLinkerController;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8098);

    @Before
    public void resetLinkNotificationToggles() {
        ReflectionTestUtils.setField(accountLinkerController, "sendLinkNotificationToEduidAccount", true);
        ReflectionTestUtils.setField(accountLinkerController, "sendLinkNotificationToLinkedAccount", true);
    }

    @Test
    public void newLinkSendsNotificationToBothAddresses() {
        Map<Object, Object> userInfo = new HashMap<>();
        userInfo.put("eduperson_principal_name", "some@institute.nl");
        userInfo.put("schac_home_organization", "mock.idp");
        userInfo.put("email", "jdoe@institute.example.nl");

        linkAccount(userInfo);

        await().atMost(TIMEOUT_SECONDS, TimeUnit.SECONDS).until(() -> greenMail.getReceivedMessages().length >= 2);
        List<MimeMessage> mimeMessages = mailMessages();
        assertEquals(2, mimeMessages.size());
        assertTrue(mimeMessages.stream().anyMatch(message -> hasRecipient(message, "mdoe@example.com")));
        assertTrue(mimeMessages.stream().anyMatch(message -> hasRecipient(message, "jdoe@institute.example.nl")));
    }

    @Test
    public void newLinkWithoutInstitutionEmailOnlyNotifiesEduIDAccount() {
        Map<Object, Object> userInfo = new HashMap<>();
        userInfo.put("eduperson_principal_name", "some@institute.nl");
        userInfo.put("schac_home_organization", "mock.idp");

        linkAccount(userInfo);

        MimeMessage mimeMessage = mailMessage();
        assertEquals(1, greenMail.getReceivedMessages().length);
        assertTrue(hasRecipient(mimeMessage, "mdoe@example.com"));
    }

    @Test
    public void eduIdNotificationDisabledByToggle() {
        ReflectionTestUtils.setField(accountLinkerController, "sendLinkNotificationToEduidAccount", false);

        Map<Object, Object> userInfo = new HashMap<>();
        userInfo.put("eduperson_principal_name", "some@institute.nl");
        userInfo.put("schac_home_organization", "mock.idp");
        userInfo.put("email", "jdoe@institute.example.nl");

        linkAccount(userInfo);

        MimeMessage mimeMessage = mailMessage();
        assertEquals(1, greenMail.getReceivedMessages().length);
        assertTrue(hasRecipient(mimeMessage, "jdoe@institute.example.nl"));
    }

    @Test
    public void institutionNotificationDisabledByToggle() {
        ReflectionTestUtils.setField(accountLinkerController, "sendLinkNotificationToLinkedAccount", false);

        Map<Object, Object> userInfo = new HashMap<>();
        userInfo.put("eduperson_principal_name", "some@institute.nl");
        userInfo.put("schac_home_organization", "mock.idp");
        userInfo.put("email", "jdoe@institute.example.nl");

        linkAccount(userInfo);

        MimeMessage mimeMessage = mailMessage();
        assertEquals(1, greenMail.getReceivedMessages().length);
        assertTrue(hasRecipient(mimeMessage, "mdoe@example.com"));
    }

    @Test
    @SneakyThrows
    public void reAuthenticatingExistingLinkDoesNotSendNotification() {
        Map<Object, Object> userInfo = new HashMap<>();
        userInfo.put("eduperson_principal_name", "some@institute.nl");
        userInfo.put("schac_home_organization", "mock.idp");
        userInfo.put("email", "jdoe@institute.example.nl");

        linkAccount(userInfo);
        await().atMost(TIMEOUT_SECONDS, TimeUnit.SECONDS).until(() -> greenMail.getReceivedMessages().length >= 2);
        purgeEmailFromAllMailboxes();

        //Re-authenticate the same institution account - this only refreshes the expiry, it is not a new link
        linkAccount(userInfo);

        assertEquals(0, greenMail.getReceivedMessages().length);
    }

    @SneakyThrows
    private void linkAccount(Map<Object, Object> userInfo) {
        String authenticationRequestId = samlAuthnRequest();
        ClientAuthenticationRequest clientAuthenticationRequest = new ClientAuthenticationRequest(authenticationRequestId, user("mdoe@example.com"), false, "response");
        given().when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(clientAuthenticationRequest)
                .put("/myconext/api/idp/generate_code_request")
                .then()
                .statusCode(HttpStatus.CREATED.value());
        //generate_code_request itself asynchronously sends a one_time_login_code mail, unrelated to the link notification under test
        //wait for it to land before purging, otherwise the purge can race the async send and leave it behind
        await().atMost(TIMEOUT_SECONDS, TimeUnit.SECONDS).until(() -> greenMail.getReceivedMessages().length >= 1);
        purgeEmailFromAllMailboxes();
        stubForTokenUserInfo(userInfo);
        String state = String.format("id=%s&user_uid=%s", authenticationRequestId, passwordEncoder.encode("mdoe"));
        given().redirects().follow(false)
                .when()
                .queryParam("code", "123456")
                .queryParam("state", state)
                .contentType(ContentType.JSON)
                .get("/myconext/api/idp/oidc/redirect")
                .then()
                .statusCode(HttpStatus.FOUND.value());
    }

}
