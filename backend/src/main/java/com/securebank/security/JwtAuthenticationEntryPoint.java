package com.securebank.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.securebank.exception.ErrorDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles unauthenticated 401 Unauthorized errors by returning structured JSON.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, 
                         HttpServletResponse response, 
                         AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ErrorDetails error = new ErrorDetails("Unauthorized: Full authentication is required to access this resource.", 
                "UNAUTHORIZED_ACCESS", authException.getMessage());
        
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
