package es.um.pds.tableros.domain.card;

import java.util.Objects;
import java.util.UUID;

public final class CardId {
    private final String value;

    public CardId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador de la tarjeta no puede estar vacío");
        }
        this.value = value;
    }

    public static CardId generate() {
        return new CardId(UUID.randomUUID().toString());
    }

    public String getValue() { 
    	return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
        	return true;
        }
        if (!(o instanceof CardId)) {
        	return false;
        }
        CardId cardId = (CardId) o;
        return Objects.equals(value, cardId.value);
    }

    @Override
    public int hashCode() { 
    	return Objects.hash(value);
    }
}
