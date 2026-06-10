package es.um.pds.tableros.domain.ports.input.card.commands;
import es.um.pds.tableros.domain.card.Etiqueta;


// Comando para crear una tarjeta (Necesita el título, tipo y dónde va a caer)
public record CrearCardCommand(
    String boardId,
    String listId,
    String titulo,
    String tipo, // "TASK" o "CHECKLIST"
    Etiqueta etiqueta  // null si no se añade etiqueta
) {}