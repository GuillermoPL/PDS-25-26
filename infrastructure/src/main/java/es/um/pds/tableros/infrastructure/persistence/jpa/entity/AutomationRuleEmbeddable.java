package es.um.pds.tableros.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Embeddable;

/**
 * @brief Componente incrustable (Embeddable) para la persistencia de reglas de automatización.
 * Representa la estructura relacional plana de una regla de automatización. Al carecer de
 * clave primaria propia, sus campos son mapeados dentro de una tabla de colección dependiente de BoardEntity.
 */
@Embeddable
public class AutomationRuleEmbeddable {
    private String id;
    private String triggerType;
    private String triggerPayload;
    private String actionType;

    /** @brief Constructor sin argumentos requerido por la especificación de JPA. */
    public AutomationRuleEmbeddable() {}

    /**
     * @brief Constructor completo de inicialización.
     * @param id Identificador único de la regla.
     * @param triggerType Tipo de evento disparador.
     * @param triggerPayload Datos adicionales del disparador.
     * @param actionType Tipo de acción a ejecutar.
     */
    public AutomationRuleEmbeddable(String id, String triggerType, String triggerPayload, String actionType) {
        this.id = id;
        this.triggerType = triggerType;
        this.triggerPayload = triggerPayload;
        this.actionType = actionType;
    }

    /** @return ID de la regla. */
    public String getId() { return id; }
    /** @param id ID de la regla. */
    public void setId(String id) { this.id = id; }
    /** @return Tipo de disparador. */
    public String getTriggerType() { return triggerType; }
    /** @param triggerType Tipo de disparador. */
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    /** @return Criterio del disparador. */
    public String getTriggerPayload() { return triggerPayload; }
    /** @param triggerPayload Criterio del disparador. */
    public void setTriggerPayload(String triggerPayload) { this.triggerPayload = triggerPayload; }
    /** @return Tipo de acción consecuente. */
    public String getActionType() { return actionType; }
    /** @param actionType Tipo de acción. */
    public void setActionType(String actionType) { this.actionType = actionType; }
}