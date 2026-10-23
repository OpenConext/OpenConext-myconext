package myconext.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Remembers in the session which client (myconext-gui or my-gui) calls the API, see {@link GuiRedirectResolver}.
 */
@Component
public class GuiRedirectFilter extends OncePerRequestFilter {

    private final GuiRedirectResolver guiRedirectResolver;

    public GuiRedirectFilter(GuiRedirectResolver guiRedirectResolver) {
        this.guiRedirectResolver = guiRedirectResolver;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.startsWith("/myconext/api/") || path.equals("/config") || path.equals("/doLogout"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        guiRedirectResolver.remember(request, guiRedirectResolver.appFromRequest(request));
        chain.doFilter(request, response);
    }
}
