package es.um.pds.tableros.domain.board;

public class TaskList {
    private final ListId id;
    private String nombre;
    private Integer maxCards; // Null si es ilimitada
    private int numCardsActual;

    public TaskList(ListId id, String nombre, Integer maxCards) {
        this.id = id;
        this.nombre = nombre;
        this.maxCards = maxCards;
        this.numCardsActual = 0;
    }

    public ListId getId() { 
    	return id;
    }
    public String getNombre() { 
    	return nombre; 
    }
    
    public boolean alcanzaLimite() {
        return maxCards != null && numCardsActual >= maxCards;
    }

    public void incrementaCards() { 
    	this.numCardsActual++; 
    }
    public void decrementaCards() { 
    	if (numCardsActual > 0) {
    		this.numCardsActual--; 
    	}
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
        	return true;
        }
        if (o == null || getClass() != o.getClass()) {
        	return false;
        }
        TaskList taskList = (TaskList) o;
        // Solo comparamos por su ListId
        return java.util.Objects.equals(id, taskList.id);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}
