package es.um.pds.tableros.domain.board;

/**
 * @brief Enumera los tipos de acciones que pueden ser ejecutadas por una regla de automatización.
 * @note Forma parte del conjunto de Value Objects que definen las reglas de automatización del tablero.
 */
public enum ActionType {
	/** Marca automáticamente la tarjeta como completada. */
    MARCAR_COMO_COMPLETADA,
    /** Añade una etiqueta de color rojo a la tarjeta para destacar su prioridad o estado. */
    AÑADIR_ETIQUETA_ROJA
}