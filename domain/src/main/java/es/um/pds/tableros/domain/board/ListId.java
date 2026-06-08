package es.um.pds.tableros.domain.board;

import java.util.UUID;

public record ListId(String value) {

    // Constructor
    public ListId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador de la lista no puede estar vacío");
        }
    }

    // Método factoría
    public static ListId generate() {
        return new ListId(UUID.randomUUID().toString());
    }
}