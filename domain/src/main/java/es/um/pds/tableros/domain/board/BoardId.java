package es.um.pds.tableros.domain.board;

import java.util.Objects;
import java.util.UUID;

public final class BoardId {
    private final String value;

    public BoardId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador del tablero no puede estar vacío");
        }
        this.value = value;
    }

    public static BoardId generate() {
        return new BoardId(UUID.randomUUID().toString());
    }

    public String getValue() { 
    	return value; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
        	return true;
        }
        if (!(o instanceof BoardId)) {
        	return false;
        }
        BoardId boardId = (BoardId) o;
        return Objects.equals(value, boardId.value);
    }

    @Override
    public int hashCode() { 
    	return Objects.hash(value); 
    }
}