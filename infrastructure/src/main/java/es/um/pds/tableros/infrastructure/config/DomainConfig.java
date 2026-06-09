package es.um.pds.tableros.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import es.um.pds.tableros.domain.services.CardMovementService;

@Configuration
public class DomainConfig {

    @Bean
    public CardMovementService cardMovementService() {
        // Spring llama a esto al arrancar y guarda el objeto como Singleton
        return new CardMovementService();
    }
}