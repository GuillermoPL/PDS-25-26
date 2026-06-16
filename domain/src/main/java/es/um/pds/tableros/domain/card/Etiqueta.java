package es.um.pds.tableros.domain.card;

/**
 * @brief Representa una marca visual o categorización para aplicar sobre una tarjeta (Value Object).
 * Al ser inmutable, garantiza que una etiqueta concreta compartida no puede ser alterada
 * sin crear una nueva instancia.
 * @param nombre Texto descriptivo de la etiqueta (ej. "Urgente", "Backend").
 * @param color Código o nombre del color para su representación en la interfaz.
 */
public record Etiqueta(String nombre, String color) {

    /**
     * @brief Constructor canónico que aplica las reglas de validación de negocio.
     * @throws IllegalArgumentException Si el nombre o el color son nulos o se encuentran en blanco.
     */
    public Etiqueta {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la etiqueta no puede estar vacío");
        }
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("El color es obligatorio");
        }
    }
}