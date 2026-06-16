package es.um.pds.tableros.domain.card;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.ListId;

/**
 * @brief Batería de pruebas unitarias para el Agregado Card.
 * Se encarga de validar la correcta inicialización de las tarjetas, la gestión
 * de sus elementos internos (etiquetas, checklist) y la integridad de su estado.
 */
class CardTest {

    private CardId cardId;
    private BoardId boardId;
    private ListId listId;

    /**
     * @brief Prepara los Value Objects necesarios para la instanciación de las tarjetas.
     */
    @BeforeEach
    void setUp() {
        cardId = new CardId("c1");
        boardId = new BoardId("b1");
        listId = new ListId("l1");
    }

    /**
     * @brief Verifica que al crear una tarjeta, sus colecciones y estados iniciales
     * se configuran por defecto de forma segura (no nulos y vacíos).
     */
    @Test
    void testCrearTarjetaInicializaCorrectamente() {
        Card card = new Card(cardId, boardId, listId, "Mi Tarjeta", CardType.TASK);
        
        assertEquals("Mi Tarjeta", card.getTitulo());
        assertFalse(card.isCompletada());
        assertTrue(card.getEtiquetas().isEmpty());
        assertTrue(card.getChecklistItems().isEmpty());
    }

    /**
     * @brief Valida que una tarjeta tipada como CHECKLIST permite la adición
     * de sub-elementos a su lista de comprobación.
     */
    @Test
    void testAnadirChecklistItemATarjetaChecklistFunciona() {
        Card card = new Card(cardId, boardId, listId, "Mi Checklist", CardType.CHECKLIST);
        
        card.anadirChecklistItem("Comprar pan");
        card.anadirChecklistItem("Comprar leche");
        
        assertEquals(2, card.getChecklistItems().size());
        assertTrue(card.getChecklistItems().contains("Comprar pan"));
    }

    /**
     * @brief Verifica la regla de negocio que prohíbe añadir elementos de checklist
     * a una tarjeta estándar (tipo TASK).
     */
    @Test
    void testAnadirChecklistItemATarjetaTareaLanzaExcepcion() {
        Card card = new Card(cardId, boardId, listId, "Mi Tarea", CardType.TASK);
        
        // Verificamos que lanza IllegalStateException si intentamos romper la regla de negocio
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            card.anadirChecklistItem("Intentar añadir a tarea");
        });
        
        assertEquals("No se pueden añadir elementos de checklist a una tarjeta de tipo tarea", exception.getMessage());
    }

    /**
     * @brief Comprueba la operativa completa sobre las etiquetas visuales.
     * Garantiza que se añaden, se borran, y que no se duplican si se intenta
     * registrar la misma etiqueta dos veces.
     */
    @Test
    void testAnadirYEliminarEtiquetas() {
        Card card = new Card(cardId, boardId, listId, "Mi Tarea", CardType.TASK);
        Etiqueta urgente = new Etiqueta("Urgente", "Rojo");
        
        card.anadirEtiqueta(urgente);
        assertEquals(1, card.getEtiquetas().size());
        
        // Añadir la misma etiqueta no debería duplicarla
        card.anadirEtiqueta(urgente);
        assertEquals(1, card.getEtiquetas().size());
        
        card.eliminarEtiqueta(urgente);
        assertTrue(card.getEtiquetas().isEmpty());
    }
}