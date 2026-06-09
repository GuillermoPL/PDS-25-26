package es.um.pds.tableros.infrastructure.persistence.jpa.entity;

import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "TASK_LIST")
public class TaskListEntity {

    @Id
    @Column(name = "ID", nullable = false)
    private String id;

    @Column(name = "NOMBRE", nullable = false) // El nombre de la lista es obligatorio
    private String nombre;

    @Column(name = "MAX_CARDS", nullable = true) // Es true porque el límite de tarjetas puede ser opcional
    private Integer maxCards;

    @Column(name = "NUM_CARDS_ACTUAL", nullable = false) // El contador siempre debe tener un valor
    private int numCardsActual;

    @ManyToOne
    @JoinColumn(name = "BOARD_ID", nullable = false)
    private BoardEntity board;

    public TaskListEntity() {}

    public TaskListEntity(String id, String nombre, Integer maxCards, int numCardsActual, BoardEntity board) {
        this.id = id;
        this.nombre = nombre;
        this.maxCards = maxCards;
        this.numCardsActual = numCardsActual;
        this.board = board;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Integer getMaxCards() { return maxCards; }
    public void setMaxCards(Integer maxCards) { this.maxCards = maxCards; }
    public int getNumCardsActual() { return numCardsActual; }
    public void setNumCardsActual(int numCardsActual) { this.numCardsActual = numCardsActual; }
    public BoardEntity getBoard() { return board; }
    public void setBoard(BoardEntity board) { this.board = board; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskListEntity)) return false;
        TaskListEntity other = (TaskListEntity) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}