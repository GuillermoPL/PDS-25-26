package es.um.pds.tableros.domain.ports.input.board.commands;

/**
 * @brief Estructura de datos (Command) para designar la lista de tareas finalizadas.
 * @param boardId Identificador del tablero sobre el que se aplica la configuración.
 * @param listId Identificador de la lista/columna que actuará como sumidero de "Done".
 */
public record DefinirListCompletadasCommand(
    String boardId,
    String listId
) {}