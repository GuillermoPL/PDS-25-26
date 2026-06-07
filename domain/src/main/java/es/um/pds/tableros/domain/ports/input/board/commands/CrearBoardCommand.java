package es.um.pds.tableros.domain.ports.input.board.commands;

// 1. Crear tablero por correo electrónico
public record CrearBoardCommand(
    String titulo,
    String emailCreator
) {
    public CrearBoardCommand {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título del tablero es obligatorio");
        }
        if (emailCreator == null || !emailCreator.contains("@")) {
            throw new IllegalArgumentException("Debe proporcionar un correo electrónico válido");
        }
    }
}