package myconext.api;

import jakarta.mail.internet.MimeMessage;
import myconext.AbstractMailBoxTest;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Base64;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FeedbackControllerTest extends AbstractMailBoxTest {

    @Test
    public void submitFeedbackWithoutScreenshot() throws Exception {
        post(Map.of(
                "message", "This is feedback",
                "url", "http://localhost/home",
                "includeScreenshot", false))
                .statusCode(HttpStatus.OK.value());

        MimeMessage mimeMessage = mailMessage();
        assertEquals("support@surfconext.nl", mimeMessage.getAllRecipients()[0].toString());
        String content = rawContent(mimeMessage);
        assertTrue(content.contains("This is feedback"));
        assertTrue(content.contains("http://localhost/home"));
    }

    @Test
    public void submitFeedbackWithScreenshot() throws Exception {
        byte[] screenshot = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
        post(Map.of(
                "message", "Feedback with screenshot",
                "url", "http://localhost/app",
                "includeScreenshot", true,
                "screenshotBase64", Base64.getEncoder().encodeToString(screenshot),
                "screenshotContentType", "image/png"))
                .statusCode(HttpStatus.OK.value());

        MimeMessage mimeMessage = mailMessage();
        assertTrue(mimeMessage.getContentType().startsWith("multipart/mixed"));
        assertTrue(rawContent(mimeMessage).contains("feedback.png"));
    }

    @Test
    public void submitFeedbackRejectsEmptyMessage() {
        post(Map.of(
                "message", "",
                "url", "http://localhost/home",
                "includeScreenshot", false))
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void submitFeedbackRejectsNonPngScreenshot() {
        post(Map.of(
                "message", "Feedback with invalid screenshot",
                "url", "http://localhost/app",
                "includeScreenshot", true,
                "screenshotBase64", Base64.getEncoder().encodeToString("not-a-png".getBytes()),
                "screenshotContentType", "text/plain"))
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    public void submitFeedbackRejectsInvalidBase64() {
        post(Map.of(
                "message", "Feedback with broken screenshot",
                "url", "http://localhost/app",
                "includeScreenshot", true,
                "screenshotBase64", "%%%not-base64%%%",
                "screenshotContentType", "image/png"))
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    private io.restassured.response.ValidatableResponse post(Map<String, Object> body) {
        return given()
                .when()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .post("/myconext/api/sp/feedback")
                .then();
    }

    private String rawContent(MimeMessage mimeMessage) throws Exception {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        mimeMessage.writeTo(out);
        return out.toString();
    }
}
