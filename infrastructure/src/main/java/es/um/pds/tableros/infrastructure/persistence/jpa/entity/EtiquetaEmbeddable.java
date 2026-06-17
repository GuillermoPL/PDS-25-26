package es.um.pds.tableros.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Embeddable;

/**
 * @brief Componente incrustable (Embeddable) para guardar colecciones de etiquetas relacionales.
 * Representa de forma plana el mapeo de base de datos para los Value Objects Etiqueta del dominio,
 * incrustándose directamente en la tabla secundaria gestionada por CardEntity.
 */
@Embeddable
public class EtiquetaEmbeddable {
    private String nombre;
    private String color;

    /** @brief Constructor sin argumentos requerido por JPA. */
    public EtiquetaEmbeddable() {}
    
    /**
     * @brief Constructor completo.
     * @param nombre Nombre o texto de la etiqueta.
     * @param color Valor cromático en formato texto.
     */
    public EtiquetaEmbeddable(String nombre, String color) {
        this.nombre = nombre;
        this.color = color;
    }
    
    /** @return El nombre de la etiqueta. */
    public String getNombre() { return nombre; }
    /** @return El color de la etiqueta. */
    public String getColor() { return color; }
}