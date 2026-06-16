package es.um.pds.tableros.domain.board;

/**
 * @brief Define el catálogo de eventos detonantores para las reglas de automatización.
 * @note Empleado como Value Object en la composición de una {@link AutomationRule}.
 */
public enum TriggerType {
    /** Evento lanzado al producirse un traslado exitoso de una tarjeta hacia una nueva columna. */
    TARJETA_MOVIDA_A_LISTA
}