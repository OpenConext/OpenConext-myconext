package surfid.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;

@RestController
public class LoginController {

    private String redirectUrl;
    private String basePath;

    public LoginController(@Value("${base_path}") String basePath, @Value("${redirect_url}") String redirectUrl) {
        this.basePath = basePath;
        this.redirectUrl = redirectUrl;
    }

    @GetMapping(value = "/config")
    public Map<String, String> config() {
        return Collections.singletonMap("loginUrl", this.basePath + "/login" );
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
        response.sendRedirect( redirectUrl);
    }

}
