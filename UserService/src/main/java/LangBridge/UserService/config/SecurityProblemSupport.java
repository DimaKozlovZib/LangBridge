package LangBridge.UserService.config;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SecurityProblemSupport implements AuthenticationEntryPoint, AccessDeniedHandler {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/problem+json");
        response.getWriter().write('{'
                + "\"type\":\"about:blank\"," 
                + "\"title\":\"Unauthorized\"," 
                + "\"status\":" + HttpStatus.UNAUTHORIZED.value() + ','
                + "\"detail\":\"Unauthorized\""
                + '}');
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, org.springframework.security.access.AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/problem+json");
        response.getWriter().write('{'
                + "\"type\":\"about:blank\"," 
                + "\"title\":\"Forbidden\"," 
                + "\"status\":" + HttpStatus.FORBIDDEN.value() + ','
                + "\"detail\":\"Access denied\""
                + '}');
    }
}
