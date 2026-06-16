package es.um.pds.tableros.domain.card;

/**
 * @brief Enumera los tipos disponibles para una tarjeta en el tablero.
 * @note Implementado como un Value Object que define el comportamiento y estructura permitida para una Card.
 */
public enum CardType { 
    /** Tarjeta estándar, usada para tareas simples con título y descripción. */
    TASK, 
    
    /** Tarjeta compleja que habilita el uso de elementos y subtareas (ChecklistItems). */
    CHECKLIST 
}