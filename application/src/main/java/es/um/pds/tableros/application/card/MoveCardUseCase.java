package es.um.pds.tableros.application.card;

import org.springframework.stereotype.Service;

@Service
public class MoveCardUseCase {
    
    // TODO: Inyectar por constructor CardRepository y BoardRepository.
    // TODO: Inyectar CardMovementService.

    public void execute(String cardIdStr, String targetListIdStr) {
        // TODO: 1. Buscar la Card en el repositorio.
        // TODO: 2. Buscar el Board asociado en el repositorio.
        // TODO: 3. Llamar a CardMovementService.moveCard(...).
        // TODO: 4. Guardar la Card actualizada en el repositorio.
    }
}