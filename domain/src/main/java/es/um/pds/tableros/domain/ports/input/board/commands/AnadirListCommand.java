package es.um.pds.tableros.domain.ports.input.board.commands;

/**
 * @brief Estructura de datos (Command) para solicitar la adición de una lista a un tablero.
 * @param boardId Identificador del tablero de destino.
 * @param nombreLista Nombre que recibirá la nueva columna.
 * @param maxCards Límite de tarjetas de la lista (null si es capacidad ilimitada).
 * @param emailSolicitante Correo del usuario que ejecuta la acción (para validación de permisos).
 * @note Implementado como Record para garantizar inmutabilidad en el transporte de datos.
 */
public record AnadirListCommand(
    String boardId,
    String nombreLista,
    Integer maxCards, // Null si es ilimitada
    String emailSolicitante // NUEVO: Identifica quién hace la petición
) {
    /**
     * @brief Constructor con validación temprana.
     * @throws IllegalArgumentException Si el ID o el nombre están vacíos, o si el límite es negativo.
     */
    public AnadirListCommand {
        if (boardId == null || boardId.isBlank()) {
            throw new IllegalArgumentException("El ID del tablero es obligatorio");
        }
        if (nombreLista == null || nombreLista.isBlank()) {
            throw new IllegalArgumentException("El nombre de la lista no puede estar vacío");
        }
        if (maxCards != null && maxCards < 0) {
            throw new IllegalArgumentException("El límite máximo de tarjetas no puede ser negativo");
        }
    }
}