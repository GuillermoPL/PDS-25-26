package es.um.pds.tableros.domain.card;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.ListId;

/**
 * @brief Raíz del Agregado (Aggregate Root) que representa una tarjeta en el sistema.
 * Centraliza las reglas de negocio sobre el contenido de una tarea, su estado de finalización,
 * su tipo, sus etiquetas y los elementos de su checklist.
 * @note Mantiene referencias (por ID) al tablero y a la lista a los que pertenece para evitar
 * acoplamiento directo entre agregados distintos.
 */
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
    private final LocalDate fechaCreacion;

    /**
     * @brief Crea una nueva tarjeta con la información básica.
     * @param id Identificador único de la tarjeta (Value Object).
     * @param boardId Identificador del tablero al que pertenece (Value Object).
     * @param ListIdActual Identificador de la lista/columna donde se ubica inicialmente (Value Object).
     * @param titulo Título breve o resumen de la tarjeta.
     * @param tipo Tipo de tarjeta (tarea estándar o checklist).
     */
    public Card(CardId id, BoardId boardId, ListId ListIdActual, String titulo, CardType tipo) {
        this.id = id;
        this.boardId = boardId;
        this.ListIdActual = ListIdActual;
        this.titulo = titulo;
        this.tipo = tipo;
        this.isCompletada = false;
        this.etiquetas = new ArrayList<>();
        this.checklistItems = new ArrayList<>();
        this.fechaCreacion = LocalDate.now();
    }

    // Getters (Retornan vistas inmutables para proteger el encapsulamiento) 

    /** @return Identificador único de la tarjeta. */
    public CardId getId() { 
        return id; 
    }
    
    /** @return Identificador del tablero contenedor. */
    public BoardId getBoardId() { 
        return boardId; 
    }
    
    /** @return Identificador de la lista donde se encuentra la tarjeta actualmente. */
    public ListId getListIdActual() { 
        return ListIdActual; 
    }
    
    /** @return Título de la tarjeta. */
    public String getTitulo() { 
        return titulo; 
    }
    
    /** @return Descripción extendida de la tarjeta. */
    public String getDescripcion() {
        return descripcion;
    }
    
    /** @return El tipo de funcionalidad de la tarjeta (TASK o CHECKLIST). */
    public CardType getTipo() { 
        return tipo; 
    }
    
    /**
     * @brief Actualiza la descripción detallada de la tarjeta.
     * @param descripcion Nuevo texto descriptivo.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    /** @return true si la tarjeta está marcada como completada, false en caso contrario. */
    public boolean isCompletada() { 
        return isCompletada; 
    }
    
    /** @return Lista inmodificable con las etiquetas (colores/categorías) asignadas a la tarjeta. */
    public List<Etiqueta> getEtiquetas() { 
        return Collections.unmodifiableList(etiquetas);
    }
    
    /** @return Lista inmodificable con los elementos de la checklist (solo válida si el tipo es CHECKLIST). */
    public List<String> getChecklistItems() { 
        return Collections.unmodifiableList(checklistItems); 
    }
    
    // Métodos de Lógica de Negocio 

    /**
     * @brief Modifica la ubicación de la tarjeta hacia una nueva lista.
     * @param newListId Identificador de la lista de destino.
     */
    public void moveTo(ListId newListId) {
        this.ListIdActual = newListId;
    }

    /**
     * @brief Cambia el estado de la tarjeta a completada.
     */
    public void marcarCompletada() {
        this.isCompletada = true;
    }

    /**
     * @brief Añade un nuevo elemento a la lista de comprobación de la tarjeta.
     * @param item Texto descriptivo del nuevo elemento de la checklist.
     * @throws IllegalStateException Si se intenta añadir a una tarjeta que no es de tipo CHECKLIST.
     */
    public void anadirChecklistItem(String item) {
        if (this.tipo != CardType.CHECKLIST) {
            throw new IllegalStateException("No se pueden añadir elementos de checklist a una tarjeta de tipo tarea");
        }
        this.checklistItems.add(item);
    }

    /**
     * @brief Asigna una nueva etiqueta visual a la tarjeta.
     * @param etiqueta Objeto etiqueta a añadir. No se añade si ya existe previamente.
     */
    public void anadirEtiqueta(Etiqueta etiqueta) {
        if (!etiquetas.contains(etiqueta)) {
            this.etiquetas.add(etiqueta);
        }
    }

    /**
     * @brief Retira una etiqueta específica de la tarjeta.
     * @param etiqueta Objeto etiqueta a eliminar.
     */
    public void eliminarEtiqueta(Etiqueta etiqueta) {
        this.etiquetas.remove(etiqueta);
    }
    
    /**
     * @brief Verifica si la tarjeta se creó hace más días de los indicados.
     * @param diasLimite Número de días a evaluar como umbral de antigüedad.
     * @return true si la fecha de creación es anterior a (hoy - diasLimite), false en caso contrario.
     */
    public boolean esAntigua(int diasLimite) {
        return this.fechaCreacion.isBefore(LocalDate.now().minusDays(diasLimite));
    }
    
    /**
     * @brief Compara la identidad de dos tarjetas.
     * @note La igualdad de los Agregados se basa única y exclusivamente en su identidad (CardId).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Card card = (Card) o;
        return java.util.Objects.equals(id, card.id);
    }

    /**
     * @brief Genera el código hash basado en la identidad de la tarjeta.
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}