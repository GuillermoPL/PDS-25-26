package es.um.pds.tableros.domain.ports.input.board.commands;

/**
 * @brief Estructura de datos (Command) para configurar la automatización dentro de un tablero.
 * @param boardId Identificador del tablero donde se aplicará la regla.
 * @param triggerType Tipo de evento disparador (ej. TARJETA_MOVIDA_A_LISTA).
 * @param triggerPayload Información contextual del disparador (ej. el ID de la lista destino).
 * @param actionType Acción automática que se ejecutará en respuesta (ej. MARCAR_COMO_COMPLETADA).
 */
public record CrearReglaCommand(
        String boardId,
        String triggerType,
        String triggerPayload,
        String actionType
    ) {}