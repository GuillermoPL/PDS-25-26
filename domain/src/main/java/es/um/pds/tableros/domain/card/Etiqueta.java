package es.um.pds.tableros.domain.card;

public record Etiqueta(String nombre, String color) {

    // Constructor
    public Etiqueta {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre de la etiqueta no puede estar vacío");
        }
        if (color == null || color.isBlank()) {
            throw new IllegalArgumentException("El color es obligatorio");
        }
    }
}