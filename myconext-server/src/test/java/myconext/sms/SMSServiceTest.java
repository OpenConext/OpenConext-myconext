package myconext.sms;

import myconext.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.Locale;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SMSServiceTest {

    @RegisterExtension
    WireMockExtension mockServer = new WireMockExtension(8381);

    private final SMSService smsService = new SMSServiceImpl("http://localhost:8381/sms", "secret");

    @Test
    void send() {
        stubFor(post(urlPathMatching("/sms")).willReturn(aResponse()
                .withStatus(200)));
        String msgNl = smsService.send("31639115008", "123456", Locale.forLanguageTag("nl"));
        assertTrue(msgNl.contains("je verificatiecode is"));

        String msgEn = smsService.send("31639115008", "123456", Locale.forLanguageTag("en"));
        assertTrue(msgEn.contains("your verification code"));
    }
}