package es.um.pds.tableros.test.application.usecases.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.um.pds.tableros.application.usecases.board.CompactacionTablerosServiceImpl;
import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.ports.output.BoardRepository;
import es.um.pds.tableros.domain.ports.output.CardRepository;

/**
 * @brief Pruebas unitarias e integración para el Servicio de Compactación (CompactacionTablerosServiceImpl).
 * Comprueba que el proceso batch identifica correctamente las tarjetas caducadas no completadas,
 * las elimina del sistema y registra los cambios en el tablero.
 */
@ExtendWith(MockitoExtension.class)
class CompactacionTablerosServiceImplTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CompactacionTablerosServiceImpl compactacionService;

    /**
     * @brief Valida que el servicio ignora las tarjetas que son recientes o que ya están completadas.
     */
    @Test
    void testIgnoraTarjetasNuevasOCompletadas() {
        // 1. Preparar Tablero
        Board board = new Board(new BoardId("b1"), "Tablero Test", new Email("test@um.es"));
        when(boardRepository.findAll()).thenReturn(List.of(board));

        // Preparar Tarjetas Simuladas (Mocks para controlar su comportamiento de tiempo)
        Card cardNueva = mock(Card.class);
        when(cardNueva.isCompletada()).thenReturn(false);
        when(cardNueva.esAntigua(7)).thenReturn(false); // NO es antigua

        Card cardCompletada = mock(Card.class);
        when(cardCompletada.isCompletada()).thenReturn(true); // Ya está completada, debe ignorarse

        when(cardRepository.findByBoardId(new BoardId("b1"))).thenReturn(List.of(cardNueva, cardCompletada));

        // 2. Ejecutar
        compactacionService.ejecutarCompactacion();

        // 3. Verificar que NO se elimina ninguna tarjeta y NO se guarda el tablero
        verify(cardRepository, never()).delete(any(CardId.class));
        verify(boardRepository, never()).save(any(Board.class));
    }

    /**
     * @brief Verifica que las tarjetas viejas no completadas se borran y el tablero se actualiza.
     */
    @Test
    void testEliminaTarjetasAntiguasYNoCompletadas() {
        // 1. Preparar Tablero con una lista válida para que no falle al recalcular
        Board board = new Board(new BoardId("b1"), "Tablero Test", new Email("test@um.es"));
        board.addList("To Do", 10);
        ListId listId = board.getTasksLists().get(0).getId();
        
        when(boardRepository.findAll()).thenReturn(List.of(board));

        // Preparar Tarjeta Simulada que SÍ cumple las condiciones para ser borrada
        Card cardVieja = mock(Card.class);
        when(cardVieja.getId()).thenReturn(new CardId("c1"));
        when(cardVieja.getListIdActual()).thenReturn(listId);
        when(cardVieja.isCompletada()).thenReturn(false); // NO completada
        when(cardVieja.esAntigua(7)).thenReturn(true);    // SÍ es antigua

        when(cardRepository.findByBoardId(new BoardId("b1"))).thenReturn(List.of(cardVieja));

        // 2. Ejecutar
        compactacionService.ejecutarCompactacion();

        // 3. Verificar que SE ELIMINA la tarjeta
        verify(cardRepository, times(1)).delete(new CardId("c1"));
        
        // Verificar que SE GUARDA el tablero para registrar el evento en el historial
        verify(boardRepository, times(1)).save(board);       
    }
}