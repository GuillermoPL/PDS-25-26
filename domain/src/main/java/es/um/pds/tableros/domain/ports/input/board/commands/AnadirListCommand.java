package es.um.pds.tableros.domain.ports.input.board.commands;

public record AnadirListCommand(
    String boardId,
    String nombreLista,
    Integer maxCards, // Null si es ilimitada
    String emailSolicitante // NUEVO: Identifica quién hace la petición
) {
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