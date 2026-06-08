package es.um.pds.tableros.domain.board;

import java.util.UUID;

public record BoardId(String value) {
    
    // Constructor
    public BoardId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador del tablero no puede estar vacío");
        }
    }

    // Método factoría
    public static BoardId generate() {
        return new BoardId(UUID.randomUUID().toString());
    }
}