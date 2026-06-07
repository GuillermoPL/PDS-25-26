package es.um.pds.tableros.domain.ports.input.card.commands;

// Comando para mover una tarjeta
public record MoverCardCommand(
    String cardId,
    String boardId,
    String targetListId
) {}