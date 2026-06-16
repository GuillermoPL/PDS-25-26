package es.um.pds.tableros.domain.board;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @brief Raíz del Agregado (Aggregate Root) que representa un tablero en el sistema.
 * Centraliza las reglas de negocio relacionadas con la gestión de listas, permisos,
 * límites de capacidad, bloqueo del tablero y reglas de automatización.
 * Actúa como límite de consistencia para sus entidades internas (TaskList).
 */
public class Board {
    private final BoardId id;
    private String titulo;
    private final Email email; // El email es único y no se puede cambiar
    private boolean isLocked;
    private final List<TaskList> tasksLists;
    private ListId listCompletadas; // Lista especial para completadas
    private final List<String> historial = new ArrayList<>();
    private final Map<Email, Rol> permisos = new HashMap<>();
    private final List<AutomationRule> reglas = new ArrayList<>();
    
    /**
     * @brief Crea un nuevo tablero con los datos fundamentales.
     * @param id Identificador único del tablero (Value Object).
     * @param titulo Título o nombre descriptivo del tablero.
     * @param email Correo electrónico del propietario (Value Object).
     */
    public Board(BoardId id, String titulo, Email email) {
        this.id = id;
        this.titulo = titulo;
        this.email = email;
        this.isLocked = false;
        this.tasksLists = new ArrayList<>();
    }

    //Getters (Retornan vistas inmutables para proteger el encapsulamiento)

    /** @return Identificador único del tablero. */
    public BoardId getId() { 
        return id; 
    }
    
    /** @return Título actual del tablero. */
    public String getTitulo() { 
        return titulo; 
    }
    
    /** @return Email del propietario del tablero. */
    public Email getEmail() { 
        return email; 
    }
    
    /** @return Lista inmodificable con el historial de eventos del tablero. */
    public List<String> getHistorial() {
        return Collections.unmodifiableList(historial);
    }
    
    /** @return true si el tablero está bloqueado temporalmente, false en caso contrario. */
    public boolean isLocked() { 
        return isLocked; 
    }
    
    /** @return Lista inmodificable de las listas de tareas (columnas) que pertenecen al tablero. */
    public List<TaskList> getTasksLists() { 
        return Collections.unmodifiableList(tasksLists);
    }
    
    /** @return Identificador de la lista designada para almacenar tarjetas completadas. */
    public ListId getListCompletadas() { 
        return listCompletadas; 
    }
    
    /** @return Mapa inmodificable con los usuarios y sus roles de acceso. */
    public Map<Email, Rol> getPermisos() {
        return Collections.unmodifiableMap(permisos);
    }
    
    /** @return Lista inmodificable de las reglas de automatización configuradas en el tablero. */
    public List<AutomationRule> getReglas() {
        return Collections.unmodifiableList(reglas);
    }
    
    //Métodos de Lógica de Negocio 
    
    /**
     * @brief Añade una nueva regla de automatización al tablero.
     * @param regla Regla a registrar en el sistema.
     */
    public void anadirRegla(AutomationRule regla) {
        this.reglas.add(regla);
    }
    
    /**
     * @brief Restaura las reglas de automatización desde la capa de persistencia.
     * @param reglasCargadas Lista de reglas extraídas de la base de datos.
     * @note Uso exclusivo por parte del Mapper de infraestructura.
     */
    public void restoreReglas(List<AutomationRule> reglasCargadas) {
        if (reglasCargadas != null) {
            this.reglas.addAll(reglasCargadas);
        }
    }

    /**
     * @brief Crea y añade una nueva lista (columna) al tablero.
     * @param nombre Nombre de la nueva lista (ej. "To Do").
     * @param maxCards Límite máximo de tarjetas permitidas en esta lista, puede ser null.
     * @return La instancia de TaskList recién creada.
     * @throws IllegalStateException Si se intenta añadir una lista estando el tablero bloqueado.
     */
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

    /**
     * @brief Define qué lista del tablero será la receptora de las tarjetas marcadas como completadas.
     * @param listId Identificador de la lista objetivo.
     */
    public void defineListCompletadas(ListId listId) {
        this.listCompletadas = listId;
    }

    /** @brief Bloquea el tablero, impidiendo la creación de listas o adición de tarjetas nuevas. */
    public void lock() { 
        this.isLocked = true;
    }
    
    /** @brief Desbloquea el tablero, restaurando su operativa normal. */
    public void unlock() { 
        this.isLocked = false; 
    }

    /**
     * @brief Verifica si las reglas de negocio permiten añadir una nueva tarjeta a una lista concreta.
     * @param targetListId ID de la lista donde se pretende insertar la tarjeta.
     * @throws IllegalStateException Si el tablero está bloqueado o si la lista ha alcanzado su límite de tarjetas.
     * @throws IllegalArgumentException Si la lista de destino proporcionada no pertenece a este tablero.
     */
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
     * @brief Verifica si se puede mover una tarjeta a una lista destino.
     * A diferencia de {@link #verificaAnadirCard}, este método PERMITE el movimiento 
     * aunque el tablero esté bloqueado, pero sigue respetando el límite de la lista de destino.
     * @param targetListId ID de la lista a la que se desea mover la tarjeta.
     * @throws IllegalStateException Si la lista de destino ha alcanzado su límite máximo de tarjetas.
     * @throws IllegalArgumentException Si la lista de destino no pertenece a este tablero.
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
     * @brief Actualiza los contadores internos de las listas cuando una tarjeta cambia de ubicación.
     * @param listaOrigenId ID de la lista de la que sale la tarjeta (puede ser nula si es de nueva creación).
     * @param listaDestinoId ID de la lista a la que entra la tarjeta.
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
     * @brief Método de reconstrucción para inyectar listas desde la persistencia.
     * No aplica reglas de negocio (no comprueba isLocked, no genera ID).
     * @param taskList Instancia de lista recuperada.
     * @note Uso exclusivo por parte del Mapper de infraestructura.
     */
    public void restoreTaskList(TaskList taskList) {
        this.tasksLists.add(taskList);
    }
    
    /**
     * @brief Registra un nuevo evento en el historial del tablero, agregando la marca temporal.
     * @param descripcion Descripción en texto plano de la acción ocurrida.
     */
    public void registrarEvento(String descripcion) {
        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        this.historial.add("[" + timestamp + "] " + descripcion);
    }
    
    /**
     * @brief Restaura el historial de eventos desde la capa de persistencia.
     * @param historialCargado Lista de Strings con los eventos históricos.
     * @note Uso exclusivo por parte del Mapper de infraestructura.
     */
    public void restoreHistorial(List<String> historialCargado) {
        if (historialCargado != null) {
            this.historial.addAll(historialCargado);
        }
    }
    
    /**
     * @brief Busca el nombre legible de una lista a partir de su identificador.
     * @param listId Identificador de la lista a buscar.
     * @return El nombre de la lista, "Origen" si listId es null, o "Lista desconocida" si no se encuentra.
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
   
    /**
     * @brief Concede acceso al tablero a un nuevo usuario con un rol determinado.
     * @param usuario Email del usuario invitado.
     * @param rol Nivel de permisos concedido (READ/WRITE).
     * @throws IllegalArgumentException Si se intenta modificar el permiso del propietario.
     */
    public void compartirCon(Email usuario, Rol rol) {
        if (usuario.equals(this.email)) {
            throw new IllegalArgumentException("El dueño ya tiene acceso total al tablero");
        }
        this.permisos.put(usuario, rol);
    }
    
    /**
     * @brief Revoca el acceso al tablero a un usuario previamente invitado.
     * @param usuario Email del usuario al que se le retira el acceso.
     * @throws IllegalArgumentException Si se intenta revocar el acceso del propietario.
     */
    public void revocarAcceso(Email usuario) {
        if (usuario.equals(this.email)) {
            throw new IllegalArgumentException("No se puede revocar el acceso al dueño del tablero");
        }
        this.permisos.remove(usuario);
    }
    
    /**
     * @brief Restaura un permiso desde la capa de persistencia.
     * @param email Correo electrónico del usuario.
     * @param rol Rol asociado al usuario.
     * @note Uso exclusivo por parte del Mapper de infraestructura.
     */
    public void restorePermiso(Email email, Rol rol) {
        this.permisos.put(email, rol);
    }
    
    /**
     * @brief Consulta el nivel de acceso que tiene un usuario específico sobre el tablero.
     * @param usuario Email a consultar.
     * @return El Rol del usuario, Rol.WRITE si es el propietario, o null si no tiene acceso.
     */
    public Rol obtenerRol(Email usuario) {
        if (usuario.equals(this.email)) {
            return Rol.WRITE; // El dueño siempre tiene acceso total implícito
        }
        return this.permisos.getOrDefault(usuario, null); // null = sin acceso
    }
    
    /**
     * @brief Compara la identidad de dos tableros.
     * @note La igualdad de los Agregados se basa única y exclusivamente en su identidad (BoardId).
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Board board = (Board) o;
        return java.util.Objects.equals(id, board.id);
    }

    /**
     * @brief Genera el código hash basado en la identidad del tablero.
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(id);
    }
}