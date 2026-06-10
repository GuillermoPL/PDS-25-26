package es.um.pds.tableros.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class EtiquetaEmbeddable {
    private String nombre;
    private String color;

    public EtiquetaEmbeddable() {}
    public EtiquetaEmbeddable(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }
    // Getters y setters...
    public String getNombre() { return nombre; }
    public String getColor() { return color; }
}