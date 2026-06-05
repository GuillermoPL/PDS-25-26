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
}
