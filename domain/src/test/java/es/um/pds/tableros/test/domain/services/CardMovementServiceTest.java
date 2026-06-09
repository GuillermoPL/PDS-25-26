package es.um.pds.tableros.test.domain.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.card.CardType;
import es.um.pds.tableros.domain.services.CardMovementService;

class CardMovementServiceTest {

    private CardMovementService movementService;
    private Board board;
    private BoardId boardId;
    
    private ListId idListaOrigen;
    private ListId idListaDestino;
    private ListId idListaCompletadas;

    @BeforeEach
    void setUp() {
        movementService = new CardMovementService();
        boardId = new BoardId("b1");
        board = new Board(boardId, "Tablero Test", new Email("test@um.es"));
        
        // Preparamos el tablero con 3 listas
        board.addList("To Do", null); // Sin límite
        board.addList("In Progress", 1); // Límite estricto de 1 tarjeta
        board.addList("Done", null); // Lista final
        
        idListaOrigen = board.getTasksLists().get(0).getId();
        idListaDestino = board.getTasksLists().get(1).getId();
        idListaCompletadas = board.getTasksLists().get(2).getId();
        
        // Definimos la última lista como la oficial de "Completadas"
        board.defineListCompletadas(idListaCompletadas);
    }

    @Test
    void testMovimientoValidoActualizaListasYContadores() {
        // 1. Creamos la tarjeta en la lista de origen
        Card card = new Card(new CardId("c1"), boardId, idListaOrigen, "Aprender DDD", CardType.TASK);
        // Simulamos que la tarjeta ya está contabilizada en el tablero
        board.registrarMovimientoTarjeta(null, idListaOrigen); 
        
        // 2. Usamos el servicio para moverla
        String traza = movementService.moveCard(card, board, idListaDestino);
        
        // 3. Comprobamos resultados
        assertEquals(idListaDestino, card.getListIdActual());
        assertEquals(0, board.getTasksLists().get(0).getNumCardsActual()); // Se restó de Origen
        assertEquals(1, board.getTasksLists().get(1).getNumCardsActual()); // Se sumó a Destino
        assertFalse(card.isCompletada()); // No está en la lista de completadas aún
        assertTrue(traza.contains("movida de la lista"));
    }

    @Test
    void testLanzaExcepcionSiTarjetaNoPerteneceAlTablero() {
        // Tarjeta asociada a un tablero "b99" distinto al nuestro ("b1")
        Card cardExtraña = new Card(new CardId("c99"), new BoardId("b99"), idListaOrigen, "Infiltrada", CardType.TASK);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            movementService.moveCard(cardExtraña, board, idListaDestino);
        });
        
        assertEquals("La tarjeta no pertenece al tablero especificado", exception.getMessage());
    }

    @Test
    void testLanzaExcepcionAlSuperarLimiteDeListaDestino() {
        Card card1 = new Card(new CardId("c1"), boardId, idListaOrigen, "Tarea 1", CardType.TASK);
        Card card2 = new Card(new CardId("c2"), boardId, idListaOrigen, "Tarea 2", CardType.TASK);
        
        // Movemos la primera tarjeta al destino (que tiene límite 1)
        movementService.moveCard(card1, board, idListaDestino);
        
        // Intentamos mover la segunda al mismo destino
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            movementService.moveCard(card2, board, idListaDestino);
        });
        
        assertTrue(exception.getMessage().contains("ha alcanzado su límite máximo"));
    }

    @Test
    void testMoverAListaCompletadasMarcaTarjetaComoCompletada() {
        Card card = new Card(new CardId("c1"), boardId, idListaOrigen, "Acabar proyecto", CardType.TASK);
        board.registrarMovimientoTarjeta(null, idListaOrigen);
        
        // Movemos a la lista especial de Completadas
        movementService.moveCard(card, board, idListaCompletadas);
        
        // Verificamos la regla especial de negocio
        assertEquals(idListaCompletadas, card.getListIdActual());
        assertTrue(card.isCompletada(), "La tarjeta debería estar marcada como completada automáticamente");
    }
}