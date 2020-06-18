package myconext.log;

import myconext.model.User;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class MDCFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) authentication;
            Object principal = token.getPrincipal();
            if (principal instanceof User) {
                User user = (User) principal;
                MDC.put("user_id", user.getId());
                MDC.put("user_email", user.getEmail());
            }
        }
        chain.doFilter(request, response);
    }
}
