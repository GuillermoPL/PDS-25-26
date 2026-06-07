package es.um.pds.tableros.domain.ports.input.board.commands;

// 4. Bloquear o desbloquear tablero
public record CambiarBloqueoBoardCommand(
    String boardId,
    boolean bloquear
) {}