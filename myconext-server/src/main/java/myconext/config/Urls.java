package myconext.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static myconext.security.GuestIdpAuthenticationRequestFilter.REGISTER_MODUS_COOKIE_NAME;

@Component
public class Urls {

    private Map<String, String> config = new HashMap<>();

    public Urls(@Value("${base_path}") String basePath,
                @Value("${base_domain}") String baseDomain,
                @Value("${my_conext_url}") String myConextUrl,
                @Value("${onegini_entity_id}") String oneGiniEntityId,
                @Value("${guest_idp_entity_id}") String guestIdpEntityId,
                @Value("${email.magic-link-url}") String magicLinkUrl,
                @Value("${domain}") String domain,
                @Value("${idp_redirect_url}") String idpBaseUrl) {
        this.config.put("loginUrl", basePath + "/login");
        this.config.put("baseDomain", baseDomain);
        this.config.put("migrationUrl", String.format("%s/Shibboleth.sso/Login?entityID=%s&target=/migration", myConextUrl, oneGiniEntityId));
        this.config.put("magicLinkUrl", magicLinkUrl);
        this.config.put("eduIDLoginUrl", String.format("%s/Shibboleth.sso/Login?entityID=%s", myConextUrl, guestIdpEntityId));
        this.config.put("eduIDRegisterUrl", String.format("%s/register", idpBaseUrl));
        this.config.put("domain", domain);
    }

    public Map<String, String> getConfig() {
        return config;
    }
}
