package es.um.pds.tableros.domain.board;

public record AutomationRule(
	    String id,
	    TriggerType triggerType,
	    String triggerPayload,
	    ActionType actionType
	) {}