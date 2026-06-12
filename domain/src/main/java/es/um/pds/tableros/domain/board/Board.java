package es.um.pds.tableros.domain.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class Board {
    private final BoardId id;
    private String titulo;
    private final Email email; //El email es único y no se puede cambiar
    private boolean isLocked;
    private final List<TaskList> tasksLists;
    private ListId listCompletadas; // Lista especial para completadas
    private final List<String> historial = new ArrayList<>();
    private final Map<Email, Rol> permisos = new HashMap<>();
    private final List<AutomationRule> reglas = new ArrayList<>();
    
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
    public List<String> getHistorial() {
        return Collections.unmodifiableList(historial);
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
    public Map<Email, Rol> getPermisos() {
        return Collections.unmodifiableMap(permisos);
    }
    public List<AutomationRule> getReglas() {
        return Collections.unmodifiableList(reglas);
    }
    
    // Y ya que estamos, añade también el método para que se puedan meter reglas:
    public void anadirRegla(AutomationRule regla) {
        this.reglas.add(regla);
    }
    
    // Método para el mapper:
    public void restoreReglas(List<AutomationRule> reglasCargadas) {
        if (reglasCargadas != null) {
            this.reglas.addAll(reglasCargadas);
        }
    }
    //Métodos
    public TaskList addList(String nombre, Integer maxCards) {
        if (isLocked) {
            throw new IllegalStateException(
                "No se pueden añadir listas a un tablero bloqueado"
            );
        }

        ListId nuevaListId = ListId.generate();
        TaskList nuevaLista = new TaskList(
            nuevaListId,
            nombre,
            maxCards
        );

        this.tasksLists.add(nuevaLista);

        return nuevaLista;
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
     * Verifica si se puede mover una tarjeta a una lista destino.
     * A diferencia de verificaAnadirCard, este método PERMITE el movimiento 
     * aunque el tablero esté bloqueado, pero sigue respetando el límite de la lista.
     */
    public void verificaMoverCard(ListId targetListId) {
        TaskList targetList = tasksLists.stream()
                .filter(l -> l.getId().equals(targetListId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("La lista de destino no pertenece a este tablero"));

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
    
    public void registrarEvento(String descripcion) {
        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        this.historial.add("[" + timestamp + "] " + descripcion);
    }
    
    public void restoreHistorial(List<String> historialCargado) {
        if (historialCargado != null) {
            this.historial.addAll(historialCargado);
        }
    }
    
    /**
     * Busca el nombre de una lista a partir de su ID.
     */
    public String obtenerNombreLista(ListId listId) {
        if (listId == null) {
        	return "Origen"; // Por si la tarjeta es nueva y viene de null
        }
        return this.tasksLists.stream()
			                  .filter(lista -> lista.getId().equals(listId))
			                  .map(TaskList::getNombre)
			                  .findFirst()
			                  .orElse("Lista desconocida");
	}
   
    public void compartirCon(Email usuario, Rol rol) {
        if (usuario.equals(this.email)) {
            throw new IllegalArgumentException("El dueño ya tiene acceso total al tablero");
        }
        this.permisos.put(usuario, rol);
    }
    
    public void revocarAcceso(Email usuario) {
        if (usuario.equals(this.email)) {
            throw new IllegalArgumentException("No se puede revocar el acceso al dueño del tablero");
        }
        this.permisos.remove(usuario);
    }
    
    public void restorePermiso(Email email, Rol rol) {
        this.permisos.put(email, rol);
    }
    
    public Rol obtenerRol(Email usuario) {
        if (usuario.equals(this.email)) {
            return Rol.WRITE; // El dueño siempre tiene acceso total implícito
        }
        return this.permisos.getOrDefault(usuario, null); // null = sin acceso
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