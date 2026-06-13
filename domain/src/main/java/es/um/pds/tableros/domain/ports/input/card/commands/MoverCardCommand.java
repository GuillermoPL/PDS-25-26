package es.um.pds.tableros.domain.ports.input.card.commands;

public record MoverCardCommand(
    String cardId,
    String boardId,
    String targetListId,
    String emailSolicitante // NUEVO
) {}