package myconext.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Decides whether a redirect back to the 'myconext' client must go to the legacy myconext-gui
 * ({@code myconext_redirect_url}) or to my-gui ({@code my_gui_redirect_url}). The choice is remembered in the
 * HTTP session by {@link GuiRedirectFilter} and the login endpoint, so that redirects from external parties
 * (OIDC callbacks) end up in the client that started the flow. Defaults to the legacy myconext-gui.
 */
@Component
public class GuiRedirectResolver {

    static final String SESSION_ATTRIBUTE = "gui_app";
    public static final String APP_MY_GUI = "my_gui";

    private final String myconextRedirectUrl;
    private final String myGuiRedirectUrl;
    private final String myConextBasePath;
    private final String myGuiBasePath;

    public GuiRedirectResolver(@Value("${myconext_redirect_url}") String myconextRedirectUrl,
                               @Value("${my_gui_redirect_url}") String myGuiRedirectUrl,
                               @Value("${myconext_base_path}") String myConextBasePath,
                               @Value("${my_gui_base_path}") String myGuiBasePath) {
        this.myconextRedirectUrl = myconextRedirectUrl;
        this.myGuiRedirectUrl = myGuiRedirectUrl;
        this.myConextBasePath = myConextBasePath;
        this.myGuiBasePath = myGuiBasePath;
    }

    /**
     * Picks the value for the client of the current request, e.g. for links in emails.
     */
    public String select(String myconextValue, String myGuiValue) {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpSession session = servletRequestAttributes.getRequest().getSession(false);
            if (session != null && APP_MY_GUI.equals(session.getAttribute(SESSION_ATTRIBUTE))) {
                return myGuiValue;
            }
        }
        return myconextValue;
    }

    /**
     * The base path of this server as seen by the client of the current request, e.g. to log in.
     */
    public String myconextBasePath() {
        return select(myConextBasePath, myGuiBasePath);
    }

    /**
     * The OIDC login callback of this server for the client of the current request, or null when the legacy
     * registered redirect-uri must be used.
     */
    public String loginRedirectUri(String registrationId) {
        String basePath = myconextBasePath();
        return basePath.equals(myConextBasePath) || !"my_conext".equals(registrationId) ? null :
                basePath + "/login/oauth2/code/" + registrationId;
    }

    public String myconextRedirectUrl() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            return myconextRedirectUrl(servletRequestAttributes.getRequest());
        }
        return myconextRedirectUrl;
    }

    public String myconextRedirectUrl(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && APP_MY_GUI.equals(session.getAttribute(SESSION_ATTRIBUTE)) ? myGuiRedirectUrl : myconextRedirectUrl;
    }

    public void remember(HttpServletRequest request, String app) {
        if (StringUtils.hasText(app)) {
            request.getSession(true).setAttribute(SESSION_ATTRIBUTE, APP_MY_GUI.equals(app) ? APP_MY_GUI : "myconext");
        }
    }

    /**
     * The client that sent the request, based on the Origin or Referer header, or null if it is not one of the two.
     */
    String appFromRequest(HttpServletRequest request) {
        String source = request.getHeader("Origin");
        if (!StringUtils.hasText(source)) {
            source = request.getHeader("Referer");
        }
        if (!StringUtils.hasText(source)) {
            return null;
        }
        if (isOrigin(source, myGuiRedirectUrl)) {
            return APP_MY_GUI;
        }
        return isOrigin(source, myconextRedirectUrl) ? "myconext" : null;
    }

    private boolean isOrigin(String source, String base) {
        return source.equals(base) || source.startsWith(base + "/");
    }
}
