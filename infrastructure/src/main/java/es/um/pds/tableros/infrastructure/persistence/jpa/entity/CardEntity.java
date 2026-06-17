package es.um.pds.tableros.infrastructure.persistence.jpa.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

/**
 * @brief Entidad de persistencia JPA mapeada a la tabla "CARD" en H2.
 * Mantiene de manera persistente las propiedades del agregado Card y almacena como
 * colecciones de elementos dependientes tanto sus etiquetas de color como los textos del checklist.
 */
@Entity
@Table(name = "CARD")
public class CardEntity {

    @Id
    @Column(name = "ID", nullable = false)
    private String id;

    @Column(name = "BOARD_ID", nullable = false)
    private String boardId;

    @Column(name = "LIST_ID_ACTUAL", nullable = false)
    private String listIdActual;

    @Column(name = "TITULO", nullable = false)
    private String titulo;

    @Column(name = "DESCRIPCION", nullable = true)
    private String descripcion;

    @Column(name = "IS_COMPLETADA", nullable = false)
    private boolean isCompletada;

    @Column(name = "TIPO", nullable = false)
    private String tipo; 

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "card_checklist_item", joinColumns = @JoinColumn(name = "card_id"))
    @Column(name = "item")
    private List<String> checklistItems = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "card_etiquetas", joinColumns = @JoinColumn(name = "card_id"))
    private List<EtiquetaEmbeddable> etiquetas = new ArrayList<>();
    
    /** @brief Constructor por defecto para JPA. */
    public CardEntity() {}

    /**
     * @brief Constructor con argumentos para inicialización.
     * @param id Clave primaria.
     * @param boardId Relación lógica con el tablero contenedor.
     * @param listIdActual Relación lógica con la columna actual.
     * @param titulo Título de la tarea.
     * @param descripcion Cuerpo de la tarea.
     * @param isCompletada Estado finalizado.
     * @param tipo Tipología en String.
     * @param checklistItems Colección de elementos del checklist.
     */
    public CardEntity(String id, String boardId, String listIdActual, String titulo, 
                      String descripcion, boolean isCompletada, String tipo, 
                      List<String> checklistItems) {
        this.id = id;
        this.boardId = boardId;
        this.listIdActual = listIdActual;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.isCompletada = isCompletada;
        this.tipo = tipo;
        this.checklistItems = checklistItems;
    }

    // --- Getters y Setters ---
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
    public boolean isCompletada() { return isCompletada; }
    public void setCompletada(boolean completada) { this.isCompletada = completada; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public List<String> getChecklistItems() { return checklistItems; }
    public void setChecklistItems(List<String> checklistItems) { this.checklistItems = checklistItems; }
    public List<EtiquetaEmbeddable> getEtiquetas() { return etiquetas; }
    public void setEtiquetas(List<EtiquetaEmbeddable> etiquetas) { this.etiquetas = etiquetas; }
    
    /** @brief Compara la igualdad de persistencia por ID de registro. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardEntity)) return false;
        CardEntity other = (CardEntity) o;
        return Objects.equals(id, other.id);
    }

    /** @brief Genera el hash code a partir del ID de la tarjeta. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}