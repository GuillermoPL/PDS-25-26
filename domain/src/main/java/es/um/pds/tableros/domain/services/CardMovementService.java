package es.um.pds.tableros.domain.services;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.card.Card;

/**
 * @brief Servicio de Dominio (Domain Service) que orquesta el movimiento de tarjetas.
 * @note En Arquitectura Hexagonal y DDD, los Servicios de Dominio se utilizan cuando una
 * regla de negocio involucra a múltiples Agregados (en este caso, Card y Board) y la lógica
 * no encaja de forma natural en ninguno de ellos por separado.
 */
public class CardMovementService {

    /**
     * @brief Constructor por defecto del servicio de dominio.
     */
    public CardMovementService() {}

    /**
     * @brief Gestiona el traslado de una tarjeta hacia una nueva lista, validando y sincronizando
     * el estado de ambos agregados implicados.
     * @param card La tarjeta que se desea mover.
     * @param board El tablero donde ocurre la acción, encargado de validar las reglas.
     * @param targetListId El identificador de la columna/lista destino.
     * @return Cadena de texto plana con la traza o resumen descriptivo de la operación realizada.
     * @throws IllegalArgumentException Si la tarjeta proporcionada no pertenece lógicamente al tablero indicado.
     * @throws IllegalStateException Si el tablero prohíbe el movimiento (ej. la lista destino está llena).
     */
    public String moveCard(Card card, Board board, ListId targetListId) {
        if (!card.getBoardId().equals(board.getId())) {
            throw new IllegalArgumentException("La tarjeta no pertenece al tablero especificado");
        }
        
        // Verificamos que podemos mover la tarjeta a la lista (límite de tarjetas en la lista)
        board.verificaMoverCard(targetListId);

        ListId oldListId = card.getListIdActual();

        // Cambiamos el estado de la Tarjeta
        card.moveTo(targetListId);

        // Actualizamos los contadores en el Tablero
        board.registrarMovimientoTarjeta(oldListId, targetListId);

        // Si pasamos la tarjeta a la lista de tareas completas la marcamos como completada
        if (board.getListCompletadas() != null && board.getListCompletadas().equals(targetListId)) {
            card.marcarCompletada();
        }
        
        String nombreOrigen = board.obtenerNombreLista(oldListId);
        String nombreDestino = board.obtenerNombreLista(targetListId);
        
        return String.format("Tarjeta '%s' movida de la lista '%s' a la lista '%s' en el tablero '%s'.", 
                card.getTitulo(), nombreOrigen, nombreDestino, board.getTitulo());
    }
}