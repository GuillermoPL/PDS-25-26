package es.um.pds.tableros.domain.card;

import java.util.UUID;

public record CardId(String value) {

    // Constructor
    public CardId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador de la tarjeta no puede estar vacío");
        }
    }

    // Método factoría
    public static CardId generate() {
        return new CardId(UUID.randomUUID().toString());
    }
}
