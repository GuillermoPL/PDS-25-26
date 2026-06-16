package es.um.pds.tableros.domain.board;

/**
 * @brief Representa una lista de tareas o columna dentro del tablero (Entidad local).
 * Actúa como una Entidad que vive de forma dependiente dentro de los límites del agregado {@link Board}.
 * Gestiona sus propios metadatos y mantiene el conteo de las tarjetas actuales para respetar el límite.
 */
public class TaskList {
    private final ListId id;
    private String nombre;
    private Integer maxCards; // Null si es ilimitada
    private int numCardsActual;

    /**
     * @brief Constructor principal utilizado por la lógica de dominio al crear nuevas listas.
     * @param id Identificador único de la lista (Value Object).
     * @param nombre Título representativo de la columna.
     * @param maxCards Capacidad máxima de la lista. Puede ser null para indicar listas infinitas.
     */
    public TaskList(ListId id, String nombre, Integer maxCards) {
        this.id = id;
        this.nombre = nombre;
        this.maxCards = maxCards;
        this.numCardsActual = 0;
    }
    
    /**
     * @brief Constructor de reconstrucción (para uso del mapper desde persistencia).
     * Permite instanciar la lista inyectando un estado de conteo previo.
     * @param id Identificador único de la lista.
     * @param nombre Título de la columna.
     * @param maxCards Límite máximo de tarjetas permitidas.
     * @param numCardsActual Número actual de tarjetas pre-existentes computadas en la persistencia.
     */
    public TaskList(ListId id, String nombre, Integer maxCards, int numCardsActual) {
        this.id = id;
        this.nombre = nombre;
        this.maxCards = maxCards;
        this.numCardsActual = numCardsActual;
    }

    /** @return Identificador único de la lista. */
    public ListId getId() { 
        return id;
    }
    
    /** @return Nombre descriptivo de la lista. */
    public String getNombre() { 
        return nombre; 
    }
    
    /** @return El límite configurado de tarjetas simultáneas, o null si es infinito. */
    public Integer getMaxCards() {
        return maxCards;
    }

    /** @return La cantidad de tarjetas registradas actualmente bajo esta lista. */
    public int getNumCardsActual() {
        return numCardsActual;
    }
    
    /**
     * @brief Evalúa si la lista se encuentra al límite de su capacidad permitida.
     * @return true si tiene límite y el conteo de tarjetas lo ha igualado o superado;
     * false en caso contrario.
     */
    public boolean alcanzaLimite() {
        return maxCards != null && numCardsActual >= maxCards;
    }

    /** @brief Incrementa el registro interno de tarjetas en la lista en una unidad. */
    public void incrementaCards() { 
        this.numCardsActual++; 
    }
    
    /** @brief Reduce el registro interno de tarjetas en una unidad (sin bajar de cero). */
    public void decrementaCards() { 
        if (numCardsActual > 0) {
            this.numCardsActual--; 
        }
    }
    
    /**
     * @brief Compara la identidad de dos listas de tareas.
     * @note La igualdad de una Entidad local se basa exclusivamente en su identificador (ListId).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaskList taskList = (TaskList) o;
        return java.util.Objects.equals(id, taskList.id);
    }

    /**
     * @brief Genera el código hash basado en el identificador de la lista.
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}