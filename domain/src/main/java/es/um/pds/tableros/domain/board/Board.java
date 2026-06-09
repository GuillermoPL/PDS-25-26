package es.um.pds.tableros.domain.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Board {
    private final BoardId id;
    private String titulo;
    private final Email email; //El email es único y no se puede cambiar
    private boolean isLocked;
    private final List<TaskList> tasksLists;
    private ListId listCompletadas; // Lista especial para completadas

    public Board(BoardId id, String titulo, Email email) {
        this.id = id;
        this.titulo = titulo;
        this.email = email;
        this.isLocked = false;
        this.tasksLists = new ArrayList<>();
    }

    //Getters
    public BoardId getId() { 
    	return id; 
    }
    public String getTitulo() { 
    	return titulo; 
    }
    public Email getEmail() { 
    	return email; 
    }
    public boolean isLocked() { 
    	return isLocked; 
    }
    public List<TaskList> getTasksLists() { 
    	return Collections.unmodifiableList(tasksLists);
    }
    public ListId getListCompletadas() { 
    	return listCompletadas; 
    }
    
    //Métodos
    public void addList(TaskList list) {
        if (isLocked) {
            throw new IllegalStateException("No se pueden añadir listas a un tablero bloqueado");
        }
        this.tasksLists.add(list);
    }

    public void defineListCompletadas(ListId listId) {
        this.listCompletadas = listId;
    }

    public void lock() { 
    	this.isLocked = true;
    }
    public void unlock() { 
    	this.isLocked = false; 
    }

    public void verificaAnadirCard(ListId targetListId) {
        if (isLocked) {
            throw new IllegalStateException("El tablero está bloqueado temporalmente. No se admiten nuevas tarjetas.");
        }
        
        TaskList targetList = tasksLists.stream()
						                .filter(l -> l.getId().equals(targetListId))
						                .findFirst()
						                .orElseThrow(() -> new IllegalArgumentException("La lista no pertenece a este tablero"));

        if (targetList.alcanzaLimite()) {
            throw new IllegalStateException("La lista '" + targetList.getNombre() + "' ha alcanzado su límite máximo de tarjetas");
        }
    }
    
    /**
     * Actualiza los contadores de las listas internas cuando una tarjeta se mueve.
     */
    public void registrarMovimientoTarjeta(ListId listaOrigenId, ListId listaDestinoId) {
        // Restamos 1 a la lista de origen (puede ser nula si la tarjeta se acaba de crear)
        if (listaOrigenId != null) {
            this.tasksLists.stream()
			                .filter(l -> l.getId().equals(listaOrigenId))
			                .findFirst()
			                .ifPresent(lista -> lista.decrementaCards());
        }

        // Sumamos 1 a la lista de destino
        this.tasksLists.stream()
			            .filter(l -> l.getId().equals(listaDestinoId))
			            .findFirst()
			            .ifPresent(lista -> lista.incrementaCards());
    }
    
    /**
     * Método de reconstrucción para uso exclusivo del mapper/repositorio.
     * No aplica reglas de negocio (no comprueba isLocked, no genera ID).
     */
    public void restoreTaskList(TaskList taskList) {
        this.tasksLists.add(taskList);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
        	return true;
        }
        if (o == null || getClass() != o.getClass()) {
        	return false;
        }
        Board board = (Board) o;
        // Solo comparamos por su BoardId
        return java.util.Objects.equals(id, board.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }

}