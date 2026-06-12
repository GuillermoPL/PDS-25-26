package es.um.pds.tableros.infrastructure;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import es.um.pds.tableros.domain.ports.input.board.CompactacionTablerosService;

@Component
public class CompactacionScheduler {

    private final CompactacionTablerosService compactacionService;

    public CompactacionScheduler(CompactacionTablerosService compactacionService) {
        this.compactacionService = compactacionService;
    }

    // Se ejecuta cada minuto (60000ms)
    @Scheduled(fixedRate = 60000) 
    public void lanzarProcesoDeLimpieza() {
        compactacionService.ejecutarCompactacion();
    }
}
