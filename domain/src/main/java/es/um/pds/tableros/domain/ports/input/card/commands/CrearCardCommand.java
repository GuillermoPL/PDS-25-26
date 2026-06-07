package es.um.pds.tableros.domain.ports.input.card.commands;

// Comando para crear una tarjeta (Necesita el título, tipo y dónde va a caer)
public record CrearCardCommand(
    String boardId,
    String listId,
    String titulo,
    String tipo // "TASK" o "CHECKLIST"
) {}