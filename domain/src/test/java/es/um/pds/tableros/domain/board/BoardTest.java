package es.um.pds.tableros.domain.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @brief Batería de pruebas unitarias para el Agregado Raíz Board.
 * Valida que las invariantes de negocio del tablero (bloqueos, límites de capacidad 
 * y gestión de listas) se cumplen estrictamente antes de persistir cualquier cambio.
 */
class BoardTest {

    private Board board;
    private BoardId boardId;
    private Email email;

    /**
     * @brief Configuración inicial para cada prueba.
     * Instancia un tablero limpio con un identificador y propietario por defecto.
     */
    @BeforeEach
    void setUp() {
        boardId = new BoardId("b1");
        email = new Email("usuario@um.es");
        board = new Board(boardId, "Tablero de Proyecto", email);
    }

    /**
     * @brief Verifica el flujo normal de creación de listas en un tablero operativo.
     * Comprueba que la lista se añade correctamente a la colección interna del agregado.
     */
    @Test
    void testAnadirListaFuncionaSiTableroDesbloqueado() {
        board.addList("To Do", 5);
        assertEquals(1, board.getTasksLists().size());
        assertEquals("To Do", board.getTasksLists().get(0).getNombre());
    }

    /**
     * @brief Valida la invariante de negocio de bloqueo del tablero.
     * Un tablero bloqueado debe rechazar la creación de nuevas listas lanzando una excepción.
     */
    @Test
    void testAnadirListaLanzaExcepcionSiTableroBloqueado() {
        board.lock();
        assertTrue(board.isLocked());
        
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            board.addList("To Do", 5);
        });
        
        assertEquals("No se pueden añadir listas a un tablero bloqueado", exception.getMessage());
    }

    /**
     * @brief Comprueba la aplicación estricta del límite WIP (Work In Progress).
     * Si una lista ha alcanzado su capacidad máxima configurada, el sistema debe 
     * impedir la entrada de nuevas tarjetas.
     */
    @Test
    void testVerificaAnadirCardRespetaLimiteLista() {
        // Añadimos una lista con un límite estricto de 2 tarjetas
        board.addList("In Progress", 2);
        TaskList lista = board.getTasksLists().get(0);
        ListId idLista = lista.getId();
        
        // Simulamos que entran 2 tarjetas a la lista (alcanzando el límite)
        board.registrarMovimientoTarjeta(null, idLista);
        board.registrarMovimientoTarjeta(null, idLista);
        
        // El tercer intento debe fallar por regla de negocio
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            board.verificaAnadirCard(idLista);
        });
        
        assertTrue(exception.getMessage().contains("ha alcanzado su límite máximo de tarjetas"));
    }

    /**
     * @brief Valida la coherencia de los contadores internos de las listas.
     * Al registrar un movimiento, la lista de origen debe decrementar su contador
     * y la de destino incrementarlo, manteniendo la consistencia de los datos.
     */
    @Test
    void testRegistrarMovimientoTarjetaActualizaContadores() {
        board.addList("Origen", null);
        board.addList("Destino", null);
        
        ListId idOrigen = board.getTasksLists().get(0).getId();
        ListId idDestino = board.getTasksLists().get(1).getId();
        
        // Metemos una tarjeta en Origen
        board.registrarMovimientoTarjeta(null, idOrigen);
        assertEquals(1, board.getTasksLists().get(0).getNumCardsActual());
        assertEquals(0, board.getTasksLists().get(1).getNumCardsActual());
        
        // La movemos de Origen a Destino
        board.registrarMovimientoTarjeta(idOrigen, idDestino);
        assertEquals(0, board.getTasksLists().get(0).getNumCardsActual());
        assertEquals(1, board.getTasksLists().get(1).getNumCardsActual());
    }
}