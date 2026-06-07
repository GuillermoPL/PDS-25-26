package es.um.pds.tableros.domain.ports.input.board.commands;

// 3. Definir la lista de completadas
public record DefinirListCompletadasCommand(
    String boardId,
    String listId
) {}