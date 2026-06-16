package es.um.pds.tableros.domain.ports.input.card.commands;

/**
 * @brief Estructura de datos (Command) para solicitar la vinculación de una etiqueta a una tarjeta.
 * @param cardId Identificador único de la tarjeta destino.
 * @param nombre Texto descriptivo que se mostrará en la etiqueta (ej. "Frontend").
 * @param color Código o identificador del color asociado a la etiqueta.
 * @note Implementado como Record para asegurar su inmutabilidad durante la petición.
 */
public record AnadirEtiquetaCommand(
    String cardId, 
    String nombre, 
    String color
) {}