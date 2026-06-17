package es.um.pds.tableros.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.um.pds.tableros.domain.services.CardMovementService;

/**
 * @brief Clase de configuración encargada de inyectar los servicios puros del dominio en Spring.
 * @note En Arquitectura Hexagonal y Domain-Driven Design (DDD), las clases del dominio no llevan anotaciones 
 * de frameworks (como \@Service o \@Component) para mantenerse puras y aisladas. Esta clase actúa como puente 
 * en la infraestructura, instanciándolas y registrándolas como Beans globales en el contenedor de Spring.
 */
@Configuration
public class DomainConfig {

    /**
     * @brief Registra e instancia el servicio de dominio CardMovementService.
     * Spring invoca este método de factoría durante el arranque de la aplicación y almacena 
     * el objeto resultante en su contenedor bajo el patrón de diseño Singleton.
     * @return Una nueva instancia del servicio de dominio puro CardMovementService.
     */
    @Bean
    public CardMovementService cardMovementService() {
        return new CardMovementService();
    }
}