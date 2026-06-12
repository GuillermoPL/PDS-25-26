package es.um.pds.tableros.domain.ports.input.board.commands;

public record CrearReglaCommand(
	    String boardId,
	    String triggerType,
	    String triggerPayload,
	    String actionType
	) {}