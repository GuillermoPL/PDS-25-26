package es.um.pds.tableros.infrastructure.rest.dto;

import java.util.List;
import java.util.Map;

/**
 * @brief Objeto de Transferencia de Datos (DTO) que representa un tablero Kanban hacia el exterior.
 * Desacopla la estructura interna del modelo de dominio mapeando los atributos a tipos primitivos 
 * y estructuras planas de Java. Es utilizado tanto por los controladores de la **API REST** para la 
 * serialización automática a JSON (Jackson) como por las vistas de **JavaFX**.
 * @note Simplifica la estructura relacional de los agregados procedentes de la base de datos **H2** mediante **JPA**,
 * transformando Value Objects complejos en cadenas de texto legibles.
 */
public class BoardDTO {
    private String id;
    private String titulo;
    private String email;
    private boolean locked;
    private List<String> nombresListas; // Enviamos solo datos planos al exterior
    private String listCompletadasId;
    private List<String> historial;
    private List<ListaDTO> listas;
    private Map<String, String> permisos;
    private List<ReglaDTO> reglas;
    
    /**
     * @brief Constructor vacío obligatorio para la deserialización automática de Jackson (JSON) en los endpoints REST.
     */
    public BoardDTO() {}

    // Getters y Setters

    /** @return Identificador único del tablero en formato String. */
    public String getId() { return id; }
    /** @param id Identificador único del tablero. */
    public void setId(String id) { this.id = id; }

    /** @return Título del tablero. */
    public String getTitulo() { return titulo; }
    /** @param titulo Título del tablero. */
    public void setTitulo(String titulo) { this.titulo = titulo; }

    /** @return Email crudo del propietario. */
    public String getEmail() { return email; }
    /** @param email Email crudo del propietario. */
    public void setEmail(String email) { this.email = email; }

    /** @return true si el tablero está bloqueado, false en caso contrario. */
    public boolean isLocked() { return locked; }
    /** @param locked Flag de estado de bloqueo. */
    public void setLocked(boolean locked) { this.locked = locked; }

    /** @return Lista con los nombres legibles de las columnas. */
    public List<String> getNombresListas() { return nombresListas; }
    /** @param nombresListas Lista con los nombres legibles de las columnas. */
    public void setNombresListas(List<String> nombresListas) { this.nombresListas = nombresListas; }

    /** @return ID de la columna configurada para tareas completadas. */
    public String getListCompletadasId() { return listCompletadasId; }
    /** @param listCompletadasId ID de la columna configurada para tareas completadas. */
    public void setListCompletadasId(String listCompletadasId) { this.listCompletadasId = listCompletadasId; }
    
    /** @return Colección de cadenas de texto con la traza histórica de auditoría. */
    public List<String> getHistorial() { return historial; }
    /** @param historial Colección de cadenas de texto con la traza histórica de auditoría. */
    public void setHistorial(List<String> historial) { this.historial = historial; }
    
    /** @return Lista de sub-DTOs con las columnas del tablero. */
    public List<ListaDTO> getListas() { return listas; }
    /** @param listas Lista de sub-DTOs con las columnas del tablero. */
    public void setListas(List<ListaDTO> listas) { this.listas = listas; }
    
    /** @return Mapa de permisos donde la clave es el email del invitado y el valor es el rol asignado. */
    public Map<String, String> getPermisos() { return permisos; }
    /** @param permisos Mapa de permisos. */
    public void setPermisos(Map<String, String> permisos) { this.permisos = permisos; }
    
    /** @return Colección de sub-DTOs con las reglas de automatización configuradas. */
    public List<ReglaDTO> getReglas() { return reglas; }
    /** @param reglas Colección de sub-DTOs con las reglas de automatización. */
    public void setReglas(List<ReglaDTO> reglas) { this.reglas = reglas; }

    /**
     * @brief Sub-DTO anidado estático que representa de forma plana una lista de tareas (columna) para el exterior.
     */
    public static class ListaDTO {
        private String id;
        private String nombre;

        /** @brief Constructor por defecto para Jackson. */
        public ListaDTO() {}
        
        /**
         * @brief Constructor parametrizado.
         * @param id Identificador único de la columna.
         * @param nombre Nombre legible de la columna.
         */
        public ListaDTO(String id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        /** @return Identificador único de la columna. */
        public String getId() { return id; }
        /** @param id Identificador de la columna. */
        public void setId(String id) { this.id = id; }
        /** @return Nombre descriptivo de la columna. */
        public String getNombre() { return nombre; }
        /** @param nombre Nombre descriptivo de la columna. */
        public void setNombre(String nombre) { this.nombre = nombre; }
    }

    /**
     * @brief Sub-DTO anidado estático que representa una regla de automatización simplificada para el exterior.
     */
    public static class ReglaDTO {
        private String id;
        private String triggerType;
        private String triggerPayload;
        private String actionType;

        /** @brief Constructor por defecto para Jackson. */
        public ReglaDTO() {}

        /**
         * @brief Constructor exhaustivo para inicialización rápida.
         * @param id ID único de la regla.
         * @param triggerType Texto del disparador (ej. "TARJETA_MOVIDA_A_LISTA").
         * @param triggerPayload Criterio de validación del disparador (ID de lista destino).
         * @param actionType Texto de la acción consecuente (ej. "MARCAR_COMO_COMPLETADA").
         */
        public ReglaDTO(String id, String triggerType, String triggerPayload, String actionType) {
            this.id = id;
            this.triggerType = triggerType;
            this.triggerPayload = triggerPayload;
            this.actionType = actionType;
        }

        /** @return ID único de la regla. */
        public String getId() { return id; }
        /** @param id ID único de la regla. */
        public void setId(String id) { this.id = id; }
        /** @return Tipo de disparador configurado. */
        public String getTriggerType() { return triggerType; }
        /** @param triggerType Tipo de disparador configurado. */
        public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
        /** @return Datos contextuales o payload del disparador. */
        public String getTriggerPayload() { return triggerPayload; }
        /** @param triggerPayload Datos contextuales del disparador. */
        public void setTriggerPayload(String triggerPayload) { this.triggerPayload = triggerPayload; }
        /** @return Tipo de acción automatizada resultante. */
        public String getActionType() { return actionType; }
        /** @param actionType Tipo de acción automatizada. */
        public void setActionType(String actionType) { this.actionType = actionType; }
    }
}