package es.um.pds.tableros.domain.board;

import java.util.UUID;

/**
 * @brief Identificador único para el agregado Board (Value Object).
 * Garantiza que cualquier ID instanciado en el dominio sea estructuralmente válido
 * y no esté vacío o nulo.
 * @param value La cadena de texto representativa del UUID.
 */
public record BoardId(String value) {
    
    /**
     * @brief Constructor con reglas de validación de negocio.
     * @throws IllegalArgumentException Si el valor proporcionado es nulo o está en blanco.
     */
    public BoardId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador del tablero no puede estar vacío");
        }
    }

    /**
     * @brief Método factoría (Factory Method) para generar nuevos identificadores seguros.
     * @return Una nueva instancia de BoardId utilizando un UUID aleatorio.
     */
    public static BoardId generate() {
        return new BoardId(UUID.randomUUID().toString());
    }
}