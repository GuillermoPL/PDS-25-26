package es.um.pds.tableros.domain.card;

import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.ListId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Card {
    private final CardId id;
    private final BoardId boardId;
    private ListId ListIdActual;
    private String titulo;
    private String descripcion;
    private boolean isCompletada;
    private final CardType tipo;
    
    private final List<Etiqueta> etiquetas;
    private final List<String> checklistItems; // Elementos si es de tipo CHECKLIST

    public Card(CardId id, BoardId boardId, ListId ListIdActual, String titulo, CardType tipo) {
        this.id = id;
        this.boardId = boardId;
        this.ListIdActual = ListIdActual;
        this.titulo = titulo;
        this.tipo = tipo;
        this.isCompletada = false;
        this.etiquetas = new ArrayList<>();
        this.checklistItems = new ArrayList<>();
    }

    // Getters
    public CardId getId() { 
    	return id; 
    }
    public BoardId getBoardId() { 
    	return boardId; 
    }
    public ListId getListIdActual() { 
    	return ListIdActual; 
    }
    public String getTitulo() { 
    	return titulo; 
    }
    public String getDescripcion() {
    	return descripcion;
    }
    public CardType getTipo() { 
    	return tipo; 
    }
    public boolean isCompletada() { 
    	return isCompletada; 
    }
    public List<Etiqueta> getEtiquetas() { 
    	return Collections.unmodifiableList(etiquetas);
    }
    public List<String> getChecklistItems() { 
    	return Collections.unmodifiableList(checklistItems); 
    }
    
    //Métodos
    public void moveTo(ListId newListId) {
        this.ListIdActual = newListId;
    }

    public void marcarCompletada() {
        this.isCompletada = true;
    }

    public void anadirChecklistItem(String item) {
        if (this.tipo != CardType.CHECKLIST) {
            throw new IllegalStateException("No se pueden añadir elementos de checklist a una tarjeta de tipo tarea");
        }
        this.checklistItems.add(item);
    }

    public void anadirEtiqueta(Etiqueta etiqueta) {
        if (!etiquetas.contains(etiqueta)) {
        	this.etiquetas.add(etiqueta);
        }
    }

    public void eliminarEtiqueta(Etiqueta etiqueta) {
        this.etiquetas.remove(etiqueta);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
        	return true;
        }
        if (o == null || getClass() != o.getClass()) {
        	return false;
        }
        Card card = (Card) o;
        // Solo comparamos por su CardId
        return java.util.Objects.equals(id, card.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

}