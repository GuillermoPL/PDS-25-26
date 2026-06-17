package es.um.pds.tableros.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import es.um.pds.tableros.infrastructure.security.AuthInterceptor;

/**
 * @brief Clase de configuración de infraestructura para el comportamiento del ciclo Web MVC de Spring.
 * Implementa la interfaz {@link WebMvcConfigurer} para alterar o añadir componentes a la red, 
 * siendo en este caso el punto central donde se activa el control de seguridad de la API REST.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    private final AuthInterceptor authInterceptor;

    /**
     * @brief Constructor que recibe el interceptor personalizado de seguridad mediante inyección de dependencias.
     * @param authInterceptor Componente encargado de auditar y autorizar los códigos de sesión HTTP.
     */
    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    /**
     * @brief Registra y configura el alcance de los interceptores HTTP en el sistema de enrutamiento.
     * Asocia de forma estricta el interceptor de seguridad sobre cualquier petición de red que coincida 
     * con el patrón de ruta global de la API REST.
     * @param registry Componente de Spring MVC empleado para matricular interceptores globales o parciales.
     * @note Aplica el patrón de rutas "/api/v1/**" para asegurar que cualquier llamada operativa sobre 
     * tableros o tarjetas quede protegida por la aduana de seguridad.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).addPathPatterns("/api/v1/**");
    }
}