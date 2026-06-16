package es.um.pds.tableros.domain.ports.input.board.commands;

/**
 * @brief Estructura de datos (Command) para alterar el estado de bloqueo de un tablero.
 * @param boardId Identificador del tablero objetivo.
 * @param bloquear Flag booleano (true para bloquear, false para desbloquear).
 * @param emailSolicitante Correo del usuario que emite la orden de bloqueo.
 */
public record CambiarBloqueoBoardCommand(
    String boardId,
    boolean bloquear,
    String emailSolicitante // NUEVO
) {}