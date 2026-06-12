package es.um.pds.tableros.infrastructure.persistence.jpa.entity;

import jakarta.persistence.Embeddable;

@Embeddable
public class AutomationRuleEmbeddable {
    private String id;
    private String triggerType;
    private String triggerPayload;
    private String actionType;

    public AutomationRuleEmbeddable() {}

    public AutomationRuleEmbeddable(String id, String triggerType, String triggerPayload, String actionType) {
        this.id = id;
        this.triggerType = triggerType;
        this.triggerPayload = triggerPayload;
        this.actionType = actionType;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
    public String getTriggerPayload() { return triggerPayload; }
    public void setTriggerPayload(String triggerPayload) { this.triggerPayload = triggerPayload; }
    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }
}