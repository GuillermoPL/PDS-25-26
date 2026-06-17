package es.um.pds.tableros.infrastructure.plantillas;

import java.util.List;

/**
 * @brief Objeto de Transferencia de Datos (DTO) empleado para mapear la estructura de plantillas YAML.
 * Sirve como un esquema estructural plano intermedio para que el procesador de Jackson descodifique 
 * y deserialice archivos de configuración de tableros predefinidos.
 */
public class PlantillaYamlDTO {
    private String titulo;
    private List<ListaYamlDTO> listas;

    /**
     * @brief Constructor explícito por defecto para Jackson.
     */
    public PlantillaYamlDTO() {}

    // Getters y Setters

    /** @return Título o nombre asignado a la plantilla del tablero. */
    public String getTitulo() { return titulo; }
    /** @param titulo Título o nombre de la plantilla del tablero. */
    public void setTitulo(String titulo) { this.titulo = titulo; }

    /** @return Colección de sub-DTOs con las columnas que estructuran la plantilla. */
    public List<ListaYamlDTO> getListas() { return listas; }
    /** @param listas Colección de sub-DTOs con las columnas que estructuran la plantilla. */
    public void setListas(List<ListaYamlDTO> listas) { this.listas = listas; }

    /**
     * @brief Sub-DTO estático anidado que define una columna e incluye los títulos de sus tareas por defecto.
     */
    public static class ListaYamlDTO {
        private String nombre;
        private List<String> tarjetas;

        /**
         * @brief Constructor explícito por defecto para la carga reflectiva de Jackson.
         */
        public ListaYamlDTO() {}

        /** @return Nombre descriptivo de la lista o columna en el YAML. */
        public String getNombre() { return nombre; }
        /** @param nombre Nombre descriptivo de la lista o columna. */
        public void setNombre(String nombre) { this.nombre = nombre; }

        /** @return Colección de cadenas de texto con los títulos de las tarjetas iniciales. */
        public List<String> getTarjetas() { return tarjetas; }
        /** @param tarjetas Colección de cadenas de texto con los títulos de las tarjetas iniciales. */
        public void setTarjetas(List<String> tarjetas) { this.tarjetas = tarjetas; }
    }
}