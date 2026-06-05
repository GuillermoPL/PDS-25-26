package es.um.pds.tableros.domain.board;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final BoardId id;
    private String titulo;
    private final String email; //El email es único y no se puede cambiar
    private boolean isLocked;
    private final List<TaskList> tasksLists;
    private ListId listCompletadas; // Lista especial para completadas

    public Board(BoardId id, String titulo, String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Debe proporcionar un correo electrónico válido");
        }
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
    public String getEmail() { 
    	return email; 
    }
    public boolean isLocked() { 
    	return isLocked; 
    }
    public List<TaskList> getTasksLists() { 
    	return new ArrayList<>(tasksLists);
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

}