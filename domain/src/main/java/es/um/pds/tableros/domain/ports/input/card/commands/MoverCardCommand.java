package es.um.pds.tableros.domain.ports.input.card.commands;

/**
 * @brief Estructura de datos (Command) que solicita el traslado de una tarjeta entre listas.
 * @param cardId Identificador de la tarjeta a desplazar.
 * @param boardId Identificador del tablero donde ocurre el movimiento (para control de reglas y bloqueo).
 * @param targetListId Identificador de la lista de destino.
 * @param emailSolicitante Correo del usuario que ordena el movimiento.
 */
public record MoverCardCommand(
    String cardId,
    String boardId,
    String targetListId,
    String emailSolicitante // NUEVO
) {}