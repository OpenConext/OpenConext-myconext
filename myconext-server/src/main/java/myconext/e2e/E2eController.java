package myconext.e2e;

import myconext.sms.SMSService;
import myconext.sms.SMSServiceMock;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

/**
 * Hooks for the end-to-end tests, only available with the e2e profile. The paths are not covered by a security
 * filter chain, so they also work when the seeded user is gone.
 */
@RestController
@Profile("e2e")
public class E2eController {

    private final SMSService smsService;
    private final E2eSeed e2eSeed;

    public E2eController(SMSService smsService, E2eSeed e2eSeed) {
        this.smsService = smsService;
        this.e2eSeed = e2eSeed;
    }

    /**
     * Restores the seeded users, the tests change and delete them. A GET, so no CSRF token is needed.
     */
    @GetMapping("/e2e/reset")
    public ResponseEntity<Void> reset() throws IOException {
        e2eSeed.seed();
        return ResponseEntity.noContent().build();
    }

    /**
     * The mocked SMS service does not send anything, so the tests read the last code sent.
     */
    @GetMapping("/e2e/sms-code")
    public ResponseEntity<Map<String, String>> smsCode() {
        if (!(smsService instanceof SMSServiceMock mock)) {
            return ResponseEntity.badRequest().build();
        }
        return mock.lastCode()
                .map(code -> ResponseEntity.ok(Map.of("code", code)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
