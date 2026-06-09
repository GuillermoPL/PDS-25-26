package es.um.pds.tableros.domain.card;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.ListId;

class CardTest {

    private CardId cardId;
    private BoardId boardId;
    private ListId listId;

    @BeforeEach
    void setUp() {
        cardId = new CardId("c1");
        boardId = new BoardId("b1");
        listId = new ListId("l1");
    }

    @Test
    void testCrearTarjetaInicializaCorrectamente() {
        Card card = new Card(cardId, boardId, listId, "Mi Tarjeta", CardType.TASK);
        
        assertEquals("Mi Tarjeta", card.getTitulo());
        assertFalse(card.isCompletada());
        assertTrue(card.getEtiquetas().isEmpty());
        assertTrue(card.getChecklistItems().isEmpty());
    }

    @Test
    void testAnadirChecklistItemATarjetaChecklistFunciona() {
        Card card = new Card(cardId, boardId, listId, "Mi Checklist", CardType.CHECKLIST);
        
        card.anadirChecklistItem("Comprar pan");
        card.anadirChecklistItem("Comprar leche");
        
        assertEquals(2, card.getChecklistItems().size());
        assertTrue(card.getChecklistItems().contains("Comprar pan"));
    }

    @Test
    void testAnadirChecklistItemATarjetaTareaLanzaExcepcion() {
        Card card = new Card(cardId, boardId, listId, "Mi Tarea", CardType.TASK);
        
        // Verificamos que lanza IllegalStateException si intentamos romper la regla de negocio
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            card.anadirChecklistItem("Intentar añadir a tarea");
        });
        
        assertEquals("No se pueden añadir elementos de checklist a una tarjeta de tipo tarea", exception.getMessage());
    }

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