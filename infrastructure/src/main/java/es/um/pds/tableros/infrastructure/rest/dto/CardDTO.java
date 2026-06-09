package es.um.pds.tableros.infrastructure.rest.dto;

import java.util.List;

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

    public CardDTO() {}

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBoardId() { return boardId; }
    public void setBoardId(String boardId) { this.boardId = boardId; }

    public String getListIdActual() { return listIdActual; }
    public void setListIdActual(String listIdActual) { this.listIdActual = listIdActual; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public List<EtiquetaDTO> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<EtiquetaDTO> etiquetas) { this.etiquetas = etiquetas; }

    public List<String> getChecklistItems() { return checklistItems; }
    public void setChecklistItems(List<String> checklistItems) { this.checklistItems = checklistItems; }

    // Sub-DTO anidado para transferir las etiquetas de forma plana
    public static class EtiquetaDTO {
        private String nombre;
        private String color;

        public EtiquetaDTO() {}
        public EtiquetaDTO(String nombre, String color) {
            this.nombre = nombre;
            this.color = color;
        }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
    }
}