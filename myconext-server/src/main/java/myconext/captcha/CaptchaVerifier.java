package myconext.captcha;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class CaptchaVerifier {

    private static final Log LOG = LogFactory.getLog(CaptchaVerifier.class);

    private final boolean captchaEnabled;
    private final String captchaHost;
    private final String captchaSiteKey;
    private final RestTemplate restTemplate;


    public CaptchaVerifier(@Value("${feature.captcha_enabled}") boolean captchaEnabled,
                           @Value("${captcha.host}") String captchaHost,
                           @Value("${captcha.sitekey}") String captchaSiteKey,
                           @Value("${captcha.apikey}") String captchaApiKey) {
        this.captchaEnabled = captchaEnabled;
        this.captchaHost = captchaHost;
        this.captchaSiteKey = captchaSiteKey;
        this.restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new ApiHeaderInterceptor(captchaApiKey));
    }

    @SuppressWarnings("unchecked")
    public boolean verify(String captchaResponse) {
        if (!captchaEnabled) {
            return true;
        }
        Map<String, String> request = Map.of("response", captchaResponse, "sitekey", this.captchaSiteKey);
        ResponseEntity<Map> responseEntity = this.restTemplate.postForEntity(this.captchaHost, request, Map.class);
        Map<String, Object> body = responseEntity.getBody();
        boolean success = (boolean) body.getOrDefault("success", false);
        if (!success) {
            LOG.warn("Unsuccessful response from captcha: " + body);
        }
        return success;
    }


}

