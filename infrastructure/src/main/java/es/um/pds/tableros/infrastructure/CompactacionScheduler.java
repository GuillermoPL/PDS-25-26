package es.um.pds.tableros.infrastructure;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.ports.input.board.CompactacionTablerosService;

/**
 * @brief Planificador automático (Scheduler) para el mantenimiento del sistema.
 * Clase de la capa de infraestructura encargada de temporizar y lanzar procesos
 * de forma asíncrona y en segundo plano, sin intervención directa del usuario.
 * @note Depende del motor de planificación de Spring (@Scheduled) y actúa como un adaptador
 * "driving" de infraestructura que invoca periódicamente a un caso de uso de aplicación.
 */
@Component
public class CompactacionScheduler {

    private final CompactacionTablerosService compactacionService;

    /**
     * @brief Constructor para la inyección del puerto de entrada del servicio de compactación.
     * @param compactacionService Servicio de aplicación encargado de purgar las tareas antiguas.
     */
    public CompactacionScheduler(CompactacionTablerosService compactacionService) {
        this.compactacionService = compactacionService;
    }

    /**
     * @brief Disparador periódico que ejecuta la limpieza de los tableros en segundo plano.
     * El método es invocado automáticamente por el framework de Spring en intervalos fijos.
     * Actualmente está configurado para ejecutarse de forma cíclica cada minuto (60.000 ms).
     */
    @Scheduled(fixedRate = 60000) 
    public void lanzarProcesoDeLimpieza() {
        compactacionService.ejecutarCompactacion();
    }
}