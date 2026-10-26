package myconext.api;

import io.swagger.v3.oas.annotations.Hidden;
import myconext.exceptions.InvalidInputException;
import myconext.mail.MailBox;
import myconext.model.User;
import myconext.repository.UserRepository;
import myconext.security.UserAuthentication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/myconext/api/sp/feedback")
@Hidden
public class FeedbackController implements UserAuthentication {

    private static final long MAX_SCREENSHOT_BYTES = 5L * 1024L * 1024L;
    private static final String SCREENSHOT_CONTENT_TYPE = "image/png";

    private final MailBox mailBox;
    private final UserRepository userRepository;

    public FeedbackController(MailBox mailBox, UserRepository userRepository) {
        this.mailBox = mailBox;
        this.userRepository = userRepository;
    }

    @Override
    public UserRepository getUserRepository() {
        return userRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> feedback(Authentication authentication, @RequestBody Map<String, Object> body) {
        User user = userFromAuthentication(authentication);
        String message = (String) body.get("message");
        String url = (String) body.get("url");
        boolean includeScreenshot = Boolean.parseBoolean(String.valueOf(body.getOrDefault("includeScreenshot", "false")));
        String screenshotBase64 = (String) body.get("screenshotBase64");
        String screenshotContentType = (String) body.getOrDefault("screenshotContentType", SCREENSHOT_CONTENT_TYPE);

        if (!StringUtils.hasText(message)) {
            throw new InvalidInputException("Feedback message is required");
        }

        byte[] screenshotBytes = null;
        String screenshotName = null;

        if (includeScreenshot && StringUtils.hasText(screenshotBase64)) {
            if (!SCREENSHOT_CONTENT_TYPE.equalsIgnoreCase(screenshotContentType)) {
                throw new InvalidInputException("Screenshot must be a PNG image");
            }
            try {
                screenshotBytes = Base64.getDecoder().decode(screenshotBase64);
            } catch (IllegalArgumentException exception) {
                throw new InvalidInputException("Screenshot is not valid base64");
            }
            if (screenshotBytes.length > MAX_SCREENSHOT_BYTES) {
                throw new InvalidInputException("Screenshot exceeds max size");
            }
            screenshotName = "feedback.png";
        }

        mailBox.sendFeedbackMail(user, message, url, screenshotBytes, screenshotName);
        return ResponseEntity.ok(Map.of("status", 200));
    }
}
