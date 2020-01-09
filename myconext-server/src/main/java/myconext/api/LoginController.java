package myconext.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    private String redirectUrl;
    private Map<String, String> config;

    public LoginController(@Value("${base_path}") String basePath,
                           @Value("${base_domain}") String baseDomain,
                           @Value("${branding}") String branding,
                           @Value("${my_conext_url}") String myConextUrl,
                           @Value("${migration_landing_page_url}") String migrationLandingPageUrl,
                           @Value("${onegini_entity_id}") String oneGiniEntityId,
                           @Value("${guest_idp_entity_id}") String guestIdpEntityId,
                           @Value("${sp_redirect_url}") String redirectUrl) {
        this.config = new HashMap<>();
        this.config.put("loginUrl", basePath + "/login");
        this.config.put("baseDomain", baseDomain);
        this.config.put("branding", branding);
        this.config.put("migrationUrl", String.format("%s/Shibboleth.sso/Login?entityID=%s&target=/startSSO?redirect_url=/migration", myConextUrl, oneGiniEntityId));
        this.config.put("myConextUrlGuestIdp", String.format("%s/Shibboleth.sso/Login?entityID=%s&target=/startSSO?redirect_url=/", myConextUrl, guestIdpEntityId));
        this.config.put("migrationLandingPageUrl", migrationLandingPageUrl);
        this.redirectUrl = redirectUrl;
    }

    @GetMapping(value = "/config")
    public Map<String, String> config() {
        return config;
    }

    @GetMapping(value = "/login")
    public void login(HttpServletRequest request, HttpServletResponse response, @RequestParam("redirect_path") String redirectPath)
            throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        request.logout();
        response.sendRedirect("/startSSO?redirect_url=" + redirectUrl + redirectPath);
    }

    @GetMapping(value = "/startSSO")
    public void startSSO(HttpServletResponse response, @RequestParam("redirect_url") String redirectUrl) throws IOException {
        redirectUrl = URLDecoder.decode(redirectUrl, Charset.defaultCharset().name());
        response.sendRedirect(redirectUrl);
    }

}
