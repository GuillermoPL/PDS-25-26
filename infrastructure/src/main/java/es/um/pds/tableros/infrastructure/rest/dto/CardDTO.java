package es.um.pds.tableros.infrastructure.rest.dto;

import java.util.List;

/**
 * @brief Objeto de Transferencia de Datos (DTO) que representa una tarjeta (tarea o checklist).
 * Empleado para aplanar los atributos de dominio de Card hacia capas de presentación, vistas locales
 * de **JavaFX** o transporte de red en los endpoints de la **API REST** en formato primitivo String/boolean.
 * @note Evita problemas de carga perezosa (LazyInitializationException) de Hibernate al mapear las colecciones
 * procedentes de la base de datos **H2** a tipos planos de transferencia segura.
 */
public class CardDTO {
    private String id;
    private String boardId;
    private String listIdActual;
    private String titulo;
    private String descripcion;
    private boolean completada;
    private String tipo; // "TASK" o "CHECKLIST"
    private List<EtiquetaDTO> etiquetas;
    private List<String> checklistItems;

    /**
     * @brief Constructor por defecto requerido para la serialización y deserialización automática de Jackson (JSON).
     */
    public CardDTO() {}

    // Getters y Setters

    /** @return Identificador de la tarjeta inyectado como String plano. */
    public String getId() { return id; }
    /** @param id Identificador de la tarjeta. */
    public void setId(String id) { this.id = id; }

    /** @return ID del tablero contenedor. */
    public String getBoardId() { return boardId; }
    /** @param boardId ID del tablero contenedor. */
    public void setBoardId(String boardId) { this.boardId = boardId; }

    /** @return ID de la lista o columna donde reside la tarjeta. */
    public String getListIdActual() { return listIdActual; }
    /** @param listIdActual ID de la lista actual. */
    public void setListIdActual(String listIdActual) { this.listIdActual = listIdActual; }

    /** @return Título de la tarjeta. */
    public String getTitulo() { return titulo; }
    /** @param titulo Título de la tarjeta. */
    public void setTitulo(String titulo) { this.titulo = titulo; }

    /** @return Cuerpo o texto descriptivo extendido. */
    public String getDescripcion() { return descripcion; }
    /** @param descripcion Cuerpo descriptivo de la tarea. */
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    /** @return true si la tarea está finalizada, false en caso contrario. */
    public boolean isCompletada() { return completada; }
    /** @param completada Flag representativo del estado de finalización. */
    public void setCompletada(boolean completada) { this.completada = completada; }

    /** @return Tipo de tarjeta en formato plano ("TASK" o "CHECKLIST"). */
    public String getTipo() { return tipo; }
    /** @param tipo Tipo de tarjeta en formato plano. */
    public void setTipo(String tipo) { this.tipo = tipo; }

    /** @return Colección de sub-DTOs con las etiquetas vinculadas. */
    public List<EtiquetaDTO> getEtiquetas() { return etiquetas; }
    /** @param etiquetas Colección de sub-DTOs con las etiquetas vinculadas. */
    public void setEtiquetas(List<EtiquetaDTO> etiquetas) { this.etiquetas = etiquetas; }

    /** @return Listado de ítems de verificación (vacío si no es de tipo CHECKLIST). */
    public List<String> getChecklistItems() { return checklistItems; }
    /** @param checklistItems Listado de ítems de verificación. */
    public void setChecklistItems(List<String> checklistItems) { this.checklistItems = checklistItems; }

    /**
     * @brief Sub-DTO anidado estático que traslada de forma simplificada una Etiqueta de dominio hacia JSON o la UI.
     */
    public static class EtiquetaDTO {
        private String nombre;
        private String color;

        /** @brief Constructor por defecto para deserialización automática. */
        public EtiquetaDTO() {}
        
        /**
         * @brief Constructor parametrizado.
         * @param nombre Nombre o categoría de la etiqueta.
         * @param color Código hexadecimal o nombre legible del color.
         */
        public EtiquetaDTO(String nombre, String color) {
            this.nombre = nombre;
            this.color = color;
        }
        
        /** @return Nombre descriptivo de la etiqueta. */
        public String getNombre() { return nombre; }
        /** @param nombre Nombre descriptivo de la etiqueta. */
        public void setNombre(String nombre) { this.nombre = nombre; }
        /** @return Código del color representativo de la etiqueta. */
        public String getColor() { return color; }
        /** @param color Código del color representativo de la etiqueta. */
        public void setColor(String color) { this.color = color; }
    }
}