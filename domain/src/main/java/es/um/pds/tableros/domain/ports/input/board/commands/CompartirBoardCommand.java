package es.um.pds.tableros.domain.ports.input.board.commands;

public record CompartirBoardCommand(
    String boardId,
    String emailSolicitante, // quien hace la petición (debe ser el dueño)
    String emailInvitado,
    String rol               // "READ" o "WRITE"
) {}