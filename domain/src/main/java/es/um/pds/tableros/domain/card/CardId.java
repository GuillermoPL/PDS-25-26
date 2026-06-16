package es.um.pds.tableros.domain.card;

import java.util.UUID;

/**
 * @brief Identificador único para el agregado Card (Value Object).
 * Garantiza que cualquier ID instanciado en el dominio sea estructuralmente válido.
 * @param value La cadena de texto representativa del UUID.
 */
public record CardId(String value) {

    /**
     * @brief Constructor canónico con reglas de validación de negocio.
     * @throws IllegalArgumentException Si el valor proporcionado es nulo o está en blanco.
     */
    public CardId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("El identificador de la tarjeta no puede estar vacío");
        }
    }

    /**
     * @brief Método factoría (Factory Method) para generar nuevos identificadores seguros.
     * @return Una nueva instancia de CardId utilizando un UUID aleatorio.
     */
    public static CardId generate() {
        return new CardId(UUID.randomUUID().toString());
    }
}