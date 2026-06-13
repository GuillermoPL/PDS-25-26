package es.um.pds.tableros.domain.ports.input.board.commands;

public record CambiarBloqueoBoardCommand(
    String boardId,
    boolean bloquear,
    String emailSolicitante // NUEVO
) {}