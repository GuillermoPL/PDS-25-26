package es.um.pds.tableros.domain.ports.input.card.commands;

import java.util.List;

/**
 * @brief Estructura de datos (Command) que encapsula todos los parámetros necesarios para crear una tarjeta.
 * @param boardId Identificador del tablero contenedor.
 * @param listId Identificador de la lista o columna donde nacerá la tarjeta.
 * @param titulo Texto principal o resumen de la tarea a crear.
 * @param tipo Tipo de la tarjeta (valores esperados: "TASK" o "CHECKLIST").
 * @param nombreEtiqueta Nombre de la etiqueta inicial a vincular (puede ser null).
 * @param colorEtiqueta Color de la etiqueta inicial (puede ser null).
 * @param checklistItems Lista de cadenas con los elementos iniciales si el tipo es CHECKLIST.
 * @param emailSolicitante Correo del usuario que ejecuta la acción de creación.
 */
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