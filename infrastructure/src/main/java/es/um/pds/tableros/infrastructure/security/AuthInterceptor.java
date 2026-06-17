package es.um.pds.tableros.infrastructure.security;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @brief Interceptor de seguridad HTTP (HandlerInterceptor) para la API REST.
 * Actúa como un filtro en la capa de infraestructura interceptando todas las peticiones
 * entrantes antes de que alcancen los controladores de Spring para validar las credenciales de sesión.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthSessionManager sessionManager;

    /**
     * @brief Construye el interceptor inyectando el gestor de sesiones activas.
     * @param sessionManager Componente que contiene el estado y tiempos de expiración de las sesiones.
     */
    public AuthInterceptor(AuthSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * @brief Intercepta la petición HTTP entrante y evalúa si el cliente tiene autorización.
     * Exime del control de seguridad a los prefijos de autenticación (/api/v1/auth) y a los mensajes 
     * de configuración CORS (OPTIONS). Para el resto, exige cabeceras de identificación válidas.
     * @param request Objeto que encapsula los datos de la petición de red del cliente.
     * @param response Objeto empleado para manipular la respuesta HTTP devuelta.
     * @param handler El objeto o controlador que está programado para recibir la petición.
     * @return true si la petición es legítima y puede continuar hacia el controlador; false si es bloqueada.
     */
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
            return false; // Bloquea la petición devolviendo un código 401
        }

        return true; // Deja pasar la petición
    }
}