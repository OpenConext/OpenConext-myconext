package myconext.api;

import myconext.config.Urls;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

import static myconext.security.GuestIdpAuthenticationRequestFilter.REGISTER_MODUS_COOKIE_NAME;

@RestController
public class LoginController {

    private final Urls urls;
    private boolean secureCookie;


    public LoginController(Urls urls,
                           @Value("${secure_cookie}") boolean secureCookie) {
        this.urls = urls;
        this.secureCookie = secureCookie;
    }

    @GetMapping("/register")
    public void register(@RequestParam(value = "lang", required = false, defaultValue = "en") String lang,
                         @RequestParam(value = "location", required = false) String location,
                         HttpServletResponse response) throws IOException {
        response.setHeader("Set-Cookie", REGISTER_MODUS_COOKIE_NAME + "=true; SameSite=Lax" + (secureCookie ? "; Secure" : ""));
        String redirectLocation = StringUtils.hasText(location) ? location : this.urls.getConfig().get("eduIDLoginUrl") + "&lang=" + lang;
        response.sendRedirect(redirectLocation);
    }

    @GetMapping("/config")
    public Map<String, String> config() {
        return this.urls.getConfig();
    }

}
