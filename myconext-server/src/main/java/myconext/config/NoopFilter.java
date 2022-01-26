package myconext.config;

import javax.servlet.*;
import java.io.IOException;

public class NoopFilter extends GenericFilter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(request, response);
    }
}
