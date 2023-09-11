package com.boha.kasietransie.filters;


import com.boha.kasietransie.util.E;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile("prod")
public class SimpleCorsFilter implements Filter {
    static final Logger log = LoggerFactory.getLogger(SimpleCorsFilter.class);
    private static final String xx = E.COFFEE+ E.COFFEE+E.COFFEE;

    public SimpleCorsFilter() {
        log.info(xx + " SimpleCorsFilter constructed");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With, remember-me");

        log.info(E.RED_DOT+" SimpleCorsFilter : about to doFilter after setting response headers: " + req.getLocalAddr());

        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

}
