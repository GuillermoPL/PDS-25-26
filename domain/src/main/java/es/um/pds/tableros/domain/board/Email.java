package es.um.pds.tableros.domain.board;

/**
 * @brief Representa un correo electrónico validado estructuralmente (Value Object).
 * Este Value Object blinda el dominio asegurando que no se pueda instanciar un correo
 * con formato incorrecto en ninguna parte de la aplicación.
 * @param value La cadena de texto que contiene el correo.
 */
public record Email(String value) {
    
    /**
     * @brief Constructor canónico que aplica las reglas de validación de formato.
     * @throws IllegalArgumentException Si el valor es nulo, está vacío, o carece de '@' o '.'.
     */
    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        if (!value.contains("@") || !value.contains(".")) {
            throw new IllegalArgumentException("El formato del correo electrónico no es válido: " + value);
        }
    }
}