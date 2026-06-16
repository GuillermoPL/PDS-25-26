package es.um.pds.tableros.domain.ports.input.board.commands;

/**
 * @brief Estructura de datos (Command) para instanciar un nuevo tablero en el sistema.
 * @param titulo Nombre descriptivo que se asignará al tablero.
 * @param emailCreator Correo electrónico del usuario que quedará registrado como propietario.
 */
public record CrearBoardCommand(
    String titulo,
    String emailCreator
) {
    /**
     * @brief Constructor con validación temprana de la información de creación.
     * @throws IllegalArgumentException Si el título está vacío o el email carece del símbolo '@'.
     */
    public CrearBoardCommand {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título del tablero es obligatorio");
        }
        if (emailCreator == null || !emailCreator.contains("@")) {
            throw new IllegalArgumentException("Debe proporcionar un correo electrónico válido");
        }
    }
}