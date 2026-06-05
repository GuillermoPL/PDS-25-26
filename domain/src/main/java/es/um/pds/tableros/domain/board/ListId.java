package es.um.pds.tableros.domain.board;

import java.util.Objects;
import java.util.UUID;

public final class ListId {
    private final String value;

    public ListId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador de la lista no puede estar vacío");
        }
        this.value = value;
    }

    public static ListId generate() {
        return new ListId(UUID.randomUUID().toString());
    }

    public String getValue() { 
    	return value; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListId)) return false;
        ListId listId = (ListId) o;
        return Objects.equals(value, listId.value);
    }

    @Override
    public int hashCode() { 
    	return Objects.hash(value); 
    }
}