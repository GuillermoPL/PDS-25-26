package es.um.pds.tableros.domain.board;

/**
 * @brief Representa una regla de automatización (Value Object) dentro de un tablero.
 * Define una condición (trigger) que, al cumplirse sobre un elemento específico (payload),
 * desencadena una acción automática (action).
 * @param id Identificador único de la regla.
 * @param triggerType El tipo de evento que dispara la regla.
 * @param triggerPayload La información adicional necesaria para evaluar el trigger (ej. el ID de la lista destino).
 * @param actionType La acción que se ejecutará cuando se dispare el trigger.
 * * @note Implementado como un Record de Java, garantizando su inmutabilidad como Value Object de Dominio.
 */
public record AutomationRule(
	    String id,
	    TriggerType triggerType,
	    String triggerPayload,
	    ActionType actionType
	) {}