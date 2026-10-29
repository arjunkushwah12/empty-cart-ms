package com.emptycart.platform.commons.logging;

import com.emptycart.platform.commons.constants.HeaderNames;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

public class CorrelationIdFilter extends OncePerRequestFilter{

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";
    public static final String MDC_USER = "userId";

    private static final Pattern VALID = Pattern.compile("^[A-Za-z0-9._-]{8,64}$");
    private static final Logger log = LoggerFactory.getLogger("http.access");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response
    , FilterChain chain) throws ServletException, IOException {

        String incoming = request.getHeader(HEADER);
        String id = (incoming!= null && VALID.matcher(incoming).matches()) ? incoming : UUID.randomUUID().toString();
        String user = request.getHeader(HeaderNames.USER_ID); //for logging only

        MDC.put(MDC_KEY, id);
        MDC.put(MDC_USER, user==null ? "-" : user);

        response.setHeader(HEADER,id);

        long start = System.currentTimeMillis();
        try{
            chain.doFilter(request, response);
        }
        finally{
            long ms = System.currentTimeMillis() - start;
            int status = response.getStatus();
            String line = "{} {} -> {} ({} ms)";
            if(status >= 500) {
                log.error(line,request.getMethod(),request.getRequestURI(),status,ms);
            } else if (status >= 400) {
                log.info(line,request.getMethod(),request.getRequestURI(),status,ms);
            }
            else {
                log.info(line,request.getMethod(),request.getRequestURI(),status,ms);
            }
            MDC.remove(MDC_KEY);
            MDC.remove(MDC_USER);
        }

    }
}
