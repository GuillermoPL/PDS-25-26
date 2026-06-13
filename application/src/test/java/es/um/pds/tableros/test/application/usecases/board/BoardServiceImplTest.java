package es.um.pds.tableros.test.application.usecases.board;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.um.pds.tableros.application.usecases.board.BoardServiceImpl;
import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.ports.input.board.commands.AnadirListCommand;
import es.um.pds.tableros.domain.ports.input.board.commands.CrearBoardCommand;
import es.um.pds.tableros.domain.ports.output.BoardRepository;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardServiceImpl boardService;

    @Test
    void testCrearNuevoTablero() {
        // 1. Preparamos el comando de entrada
        CrearBoardCommand cmd = new CrearBoardCommand("Mi Tablero", "usuario@um.es");

        // 2. Llamamos a nuestro caso de uso
        Board tableroCreado = boardService.crearNuevoTablero(cmd);

        // 3. Comprobamos que el servicio montó bien el objeto
        assertNotNull(tableroCreado.getId());
        assertEquals("Mi Tablero", tableroCreado.getTitulo());
        assertEquals("usuario@um.es", tableroCreado.getEmail().value());

        // Verificamos que el servicio haya llamado al repositorio para guardar el tablero (1 sola vez)
        verify(boardRepository, times(1)).save(any(Board.class));
    }

    @Test
    void testAnadirListaATableroExistente() {
        // 1. Preparamos un tablero simulado que el mock nos devolverá
        Board boardSimulado = new Board(new BoardId("b1"), "Tablero Mock", new Email("test@um.es"));
        when(boardRepository.findById(new BoardId("b1"))).thenReturn(Optional.of(boardSimulado));

        AnadirListCommand cmd = new AnadirListCommand("b1", "Lista Nueva", 5, "test@um.es");

        // 2. Ejecutamos el servicio
        boardService.anadirListaATablero(cmd);

        // 3. Verificamos que la lista se añadió al agregado
        assertEquals(1, boardSimulado.getTasksLists().size());
        assertEquals("Lista Nueva", boardSimulado.getTasksLists().get(0).getNombre());

        // Verificamos que se guardaron los cambios en la base de datos
        verify(boardRepository, times(1)).save(boardSimulado);
    }

    @Test
    void testAnadirListaLanzaExcepcionSiTableroNoExiste() {
        // Configuramos el mock para simular que no encuentra el tablero
        when(boardRepository.findById(any(BoardId.class))).thenReturn(Optional.empty());

        AnadirListCommand cmd = new AnadirListCommand("b_inexistente", "Lista", null, "test@um.es");
        
        // Verificamos que el servicio atrapa esto y lanza el error correcto
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            boardService.anadirListaATablero(cmd);
        });
        
        assertEquals("El tablero especificado no existe", exception.getMessage());
        // Verificamos que JAMÁS se intentó guardar nada
        verify(boardRepository, never()).save(any());
    }
}
