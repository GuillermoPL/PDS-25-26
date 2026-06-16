package es.um.pds.tableros.domain.ports.input.board.commands;

/**
 * @brief Estructura de datos (Command) para gestionar las invitaciones a un tablero.
 * @param boardId Identificador del tablero a compartir.
 * @param emailSolicitante Correo de quien hace la petición (debe poseer permisos suficientes).
 * @param emailInvitado Correo del usuario que recibirá el acceso.
 * @param rol Cadena representativa del nivel de acceso concedido (ej. "READ" o "WRITE").
 */
public record CompartirBoardCommand(
    String boardId,
    String emailSolicitante, // quien hace la petición (debe ser el dueño)
    String emailInvitado,
    String rol               // "READ" o "WRITE"
) {}