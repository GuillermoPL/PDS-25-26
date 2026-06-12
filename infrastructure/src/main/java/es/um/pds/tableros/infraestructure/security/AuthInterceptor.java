package es.um.pds.tableros.infraestructure.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthSessionManager sessionManager;

    public AuthInterceptor(AuthSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Dejamos pasar peticiones OPTIONS (CORS) y el endpoint de login
        if (request.getMethod().equals("OPTIONS") || request.getRequestURI().contains("/api/v1/auth")) {
            return true;
        }

        String email = request.getHeader("X-User-Email");
        String codigo = request.getHeader("X-Auth-Code"); // NUEVA CABECERA

        if (email == null || codigo == null || !sessionManager.verificarYRenovarCodigo(email, codigo)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false; // Bloquea la petición
        }

        return true; // Deja pasar la petición
    }
}