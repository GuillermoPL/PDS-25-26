package es.um.pds.tableros.domain.board;

public record Email(String value) {
    
    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (!value.contains("@") || !value.contains(".")) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido: " + value);
        }
    }
}