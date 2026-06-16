package es.um.pds.tableros.domain.board;

import java.util.UUID;

/**
 * @brief Identificador único para una lista de tareas o columna (Value Object).
 * Garantiza que cualquier ID de lista instanciado en el dominio sea estructuralmente válido
 * y no esté vacío o nulo.
 * @param value La cadena de texto representativa del UUID.
 */
public record ListId(String value) {

    /**
     * @brief Constructor con reglas de validación de negocio.
     * @throws IllegalArgumentException Si el valor proporcionado es nulo o está en blanco.
     */
    public ListId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador de la lista no puede estar vacío");
        }
    }

    /**
     * @brief Método factoría (Factory Method) para generar nuevos identificadores seguros.
     * @return Una nueva instancia de ListId utilizando un UUID aleatorio.
     */
    public static ListId generate() {
        return new ListId(UUID.randomUUID().toString());
    }
}