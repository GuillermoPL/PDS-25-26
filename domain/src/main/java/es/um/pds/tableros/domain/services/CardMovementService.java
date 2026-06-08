package es.um.pds.tableros.domain.services;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.card.Card;

public class CardMovementService {
    /**
     * Mueve una tarjeta de lista validando las invariantes de negocio del tablero.
     * @return String representación de la traza.
     */
	public String moveCard(Card card, Board board, ListId targetListId) {
        if (!card.getBoardId().equals(board.getId())) {
            throw new IllegalArgumentException("La tarjeta no pertenece al tablero especificado");
        }
         //Verificamos que podemos mover la tarjeta a la lista (límite de tarjetas en la lista)
        board.verificaAnadirCard(targetListId);

        ListId oldListId = card.getListIdActual();

        // Cambiamos el estado de la Tarjeta
        card.moveTo(targetListId);

        // Actualizamos los contadores en el Tablero
        board.registrarMovimientoTarjeta(oldListId, targetListId);

        //Si pasamos la tarjeta a la lista de tareas completas la marcamos como completada
        if (board.getListCompletadas() != null && board.getListCompletadas().equals(targetListId)) {
            card.marcarCompletada();
        }

        String traceLog = String.format("Tarjeta [%s] movida de la lista [%s] a la lista [%s] en el tablero [%s].", 
        		card.getId().value(), oldListId.value(), targetListId.value(), board.getId().value());
        
        return traceLog;
    }
}