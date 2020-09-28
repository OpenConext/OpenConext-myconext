package myconext.log;

import myconext.model.User;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

public class MDCFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        MDCAdapter mdcAdapter = MDC.getMDCAdapter();
        mdcAdapter.clear();

        MDC.put("Sessionid", UUID.randomUUID().toString());
        
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
