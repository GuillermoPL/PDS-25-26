package es.um.pds.tableros.application.usecases.events;

/**
 * @brief Evento de Aplicación (Application Event) que notifica el traslado exitoso de una tarjeta.
 * Se publica a través del bus de eventos de Spring para que otros servicios 
 * (como la automatización) reaccionen sin acoplarse directamente al CardService.
 * @param boardId Identificador del tablero donde ocurrió el movimiento.
 * @param cardId Identificador de la tarjeta movida.
 * @param listaDestinoId Identificador de la lista final donde aterrizó la tarjeta.
 */
public record CardMovidaEvent(
        String boardId,
        String cardId,
        String listaDestinoId
    ) {}