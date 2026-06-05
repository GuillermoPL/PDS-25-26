package es.um.pds.tableros.domain.card;

import java.util.Objects;

public final class Etiqueta {
    private final String nombre;
    private final String color; // Almacena el código hexadecimal o nombre del color

    public Etiqueta(String nombre, String color) {
        if (nombre == null || nombre.isBlank()) {
        	throw new IllegalArgumentException("El nombre de la etiqueta no puede estar vacío");
        }
        if (color == null || color.isBlank()) {
        	throw new IllegalArgumentException("El color es obligatorio");
        }
        this.nombre = nombre;
        this.color = color;
    }

    public String getName() { 
    	return nombre; 
    }
    public String getColor() { 
    	return color; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
        	return true;
        }
        if (!(o instanceof Etiqueta)) {
        	return false;
        }
        Etiqueta etiqueta = (Etiqueta) o;
        return Objects.equals(nombre, etiqueta.nombre) && Objects.equals(color, etiqueta.color);
    }

    @Override
    public int hashCode() { 
    	return Objects.hash(nombre, color); 
    }
}