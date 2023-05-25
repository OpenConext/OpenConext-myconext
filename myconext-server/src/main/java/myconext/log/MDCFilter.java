package myconext.log;

import myconext.model.User;
import org.slf4j.MDC;
import org.slf4j.spi.MDCAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static myconext.log.MDCContext.USER_ID;

public class MDCFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        MDCAdapter mdcAdapter = MDC.getMDCAdapter();
        mdcAdapter.clear();

        HttpSession session = request.getSession(false);
        if (session != null) {
            mdcAdapter.put("session_id", session.getId());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof PreAuthenticatedAuthenticationToken) {
            PreAuthenticatedAuthenticationToken token = (PreAuthenticatedAuthenticationToken) authentication;
            Object principal = token.getPrincipal();
            if (principal instanceof User) {
                User user = (User) principal;
                mdcAdapter.put(USER_ID, user.getEmail());
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            mdcAdapter.clear();
        }


    }
}
