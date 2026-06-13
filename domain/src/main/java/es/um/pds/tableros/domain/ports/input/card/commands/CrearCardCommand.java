package es.um.pds.tableros.domain.ports.input.card.commands;

import java.util.List;

public record CrearCardCommand(
    String boardId,
    String listId,
    String titulo,
    String tipo, // "TASK" o "CHECKLIST"
    String nombreEtiqueta, // NUEVO: en lugar de objeto Etiqueta
    String colorEtiqueta,  // NUEVO: en lugar de objeto Etiqueta
    List<String> checklistItems,
    String emailSolicitante // NUEVO
) {}