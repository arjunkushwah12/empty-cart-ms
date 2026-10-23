package com.emptycart.gateway.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class JsonSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        write(req, res, 401, "Unauthorized", "Authentication required or token invalid");
    }

    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        write(req, res, 403, "Forbidden", "You do not have permission to access this resource");
    }

    private void write(HttpServletRequest req, HttpServletResponse res, int status, String error, String message) throws IOException {
        String path = req.getRequestURI().replace("\\", "\\\\").replace("\"", "\\\"");
        res.setStatus(status);
        res.setContentType("application/json");
        res.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":%d,\"error\":\"%s\",\"message\":\"%s\",\"path\":\"%s\",\"errors\":null}",
                Instant.now(), status, error, message, path));
    }
}