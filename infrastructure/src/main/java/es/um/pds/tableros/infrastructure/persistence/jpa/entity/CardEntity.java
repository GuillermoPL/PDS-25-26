package es.um.pds.tableros.infrastructure.persistence.jpa.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

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

    @ElementCollection
    @CollectionTable(name = "CARD_CHECKLIST_ITEM", joinColumns = @JoinColumn(name = "CARD_ID"))
    @Column(name = "ITEM", nullable = false)
    private List<String> checklistItems = new ArrayList<>();

    public CardEntity() {}

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
    public boolean isCompletada() { return isCompletada; }
    public void setCompletada(boolean completada) { this.isCompletada = completada; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public List<String> getChecklistItems() { return checklistItems; }
    public void setChecklistItems(List<String> checklistItems) { this.checklistItems = checklistItems; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CardEntity)) return false;
        CardEntity other = (CardEntity) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}