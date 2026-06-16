package es.um.pds.tableros.test.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

/**
 * @brief Pruebas de integración a nivel de Dominio para el CardMovementService.
 * Valida la correcta interacción y sincronización entre los agregados Card y Board
 * cuando ocurre una transferencia compleja (movimiento de tarjetas entre listas).
 */
class CardMovementServiceTest {

    private CardMovementService movementService;
    private Board board;
    private BoardId boardId;
    
    private ListId idListaOrigen;
    private ListId idListaDestino;
    private ListId idListaCompletadas;

    /**
     * @brief Prepara un escenario complejo de pruebas con un tablero,
     * múltiples listas con diferentes límites y una lista designada como "Completada".
     */
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

    /**
     * @brief Valida un traslado exitoso.
     * Comprueba que la tarjeta adquiere el nuevo ID de lista y que el tablero
     * balancea correctamente los contadores de las columnas implicadas.
     */
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

    /**
     * @brief Verifica la protección de consistencia lógica cruzada.
     * El servicio debe rechazar mover una tarjeta sobre un tablero al que no pertenece.
     */
    @Test
    void testLanzaExcepcionSiTarjetaNoPerteneceAlTablero() {
        // Tarjeta asociada a un tablero "b99" distinto al nuestro ("b1")
        Card cardExtraña = new Card(new CardId("c99"), new BoardId("b99"), idListaOrigen, "Infiltrada", CardType.TASK);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            movementService.moveCard(cardExtraña, board, idListaDestino);
        });
        
        assertEquals("La tarjeta no pertenece al tablero especificado", exception.getMessage());
    }

    /**
     * @brief Valida que el servicio de dominio delega y respeta las invariantes
     * del tablero, impidiendo un movimiento si la lista de destino está llena.
     */
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

    /**
     * @brief Comprueba el efecto automatizado del dominio.
     * Al mover una tarjeta a la lista configurada como "Completadas", la tarjeta
     * debe auto-marcarse como completada a nivel interno.
     */
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