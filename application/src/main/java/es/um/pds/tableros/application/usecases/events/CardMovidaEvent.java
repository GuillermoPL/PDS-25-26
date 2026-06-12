package es.um.pds.tableros.application.usecases.events;

public record CardMovidaEvent(
	    String boardId,
	    String cardId,
	    String listaDestinoId
	) {}