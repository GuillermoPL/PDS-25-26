package es.um.pds.tableros.infrastructure.plantillas;

import java.util.List;

public class PlantillaYamlDTO {
    private String titulo;
    private List<ListaYamlDTO> listas;

    // Getters y Setters obligatorios para Jackson
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public List<ListaYamlDTO> getListas() { return listas; }
    public void setListas(List<ListaYamlDTO> listas) { this.listas = listas; }

    public static class ListaYamlDTO {
        private String nombre;
        private List<String> tarjetas;

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public List<String> getTarjetas() { return tarjetas; }
        public void setTarjetas(List<String> tarjetas) { this.tarjetas = tarjetas; }
    }
}