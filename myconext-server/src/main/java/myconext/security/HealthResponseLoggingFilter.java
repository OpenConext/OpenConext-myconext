package myconext.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class HealthResponseLoggingFilter extends OncePerRequestFilter {

    private static final Log LOG = LogFactory.getLog(HealthResponseLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"/internal/health".equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingResponseWrapper wrappedResponse =
                new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(request, wrappedResponse);
        } finally {
            int status = wrappedResponse.getStatus();
            if (status != HttpServletResponse.SC_OK) {
                String body = new String(wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

                LOG.info(String.format("Health endpoint returned HTTP %s: %s", status, body));
            }

            wrappedResponse.copyBodyToResponse();
        }
    }
}
