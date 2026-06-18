package es.um.pds.tableros.test.application.usecases.board;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import es.um.pds.tableros.domain.ports.input.board.commands.RenombrarBoardCommand;
import es.um.pds.tableros.domain.ports.output.BoardRepository;

/**
 * @brief Pruebas unitarias para el Servicio de Aplicación de Tableros (BoardServiceImpl).
 * Utiliza Mockito para simular el comportamiento de la capa de infraestructura (Repositorio)
 * y validar exclusivamente la lógica de orquestación de los casos de uso.
 */
@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {

    @Mock
    private BoardRepository boardRepository;

    @InjectMocks
    private BoardServiceImpl boardService;

    /**
     * @brief Verifica el caso de uso de creación de un nuevo tablero.
     * Comprueba que se instancia correctamente el agregado de dominio y que
     * se invoca el método de guardado en el repositorio simulado.
     */
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

    /**
     * @brief Valida la adición de una lista a un tablero ya existente.
     * Comprueba que el servicio recupera el tablero, inyecta la lista y persiste los cambios.
     */
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

    /**
     * @brief Verifica el manejo de errores cuando se opera sobre un tablero inexistente.
     * Garantiza que el servicio lanza la excepción adecuada y bloquea cualquier
     * intento de escritura en la base de datos.
     */
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
    
    /**
     * @brief Verifica el caso de éxito al renombrar un tablero.
     * Comprueba que si el usuario es el propietario, el título se actualiza
     * y se guardan los cambios en el repositorio.
     */
    @Test
    void testRenombrarTableroExitoComoDueno() {
        // 1. Preparamos el mock con el título original
        Board boardSimulado = new Board(new BoardId("b1"), "Título Viejo", new Email("dueno@um.es"));
        when(boardRepository.findById(new BoardId("b1"))).thenReturn(Optional.of(boardSimulado));

        // 2. Ejecutamos el comando simulando ser el dueño
        RenombrarBoardCommand cmd = new RenombrarBoardCommand("b1", "Título Nuevo", "dueno@um.es");
        boardService.renombrarTablero(cmd);

        // 3. Verificamos que el título cambió y se llamó a guardar
        assertEquals("Título Nuevo", boardSimulado.getTitulo());
        verify(boardRepository, times(1)).save(boardSimulado);
    }

    /**
     * @brief Valida la seguridad del caso de uso renombrar tablero.
     * Garantiza que si un intruso (sin permisos) intenta cambiar el nombre,
     * el sistema lanza una excepción y no guarda nada en la base de datos.
     */
    @Test
    void testRenombrarTableroLanzaExcepcionSiNoTienePermisos() {
        // 1. Preparamos el mock
        Board boardSimulado = new Board(new BoardId("b1"), "Título Viejo", new Email("dueno@um.es"));
        when(boardRepository.findById(new BoardId("b1"))).thenReturn(Optional.of(boardSimulado));

        // 2. Ejecutamos el comando simulando ser un usuario que NO es el dueño ni tiene permisos
        RenombrarBoardCommand cmd = new RenombrarBoardCommand("b1", "Título Hackeado", "intruso@um.es");

        // 3. Verificamos que salta la excepción de seguridad
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            boardService.renombrarTablero(cmd);
        });

        // 4. Comprobamos que el mensaje es correcto y que NUNCA se guardó el tablero modificado
        assertEquals("Solo los usuarios con permiso de escritura pueden modificar el tablero.", exception.getMessage());
        assertEquals("Título Viejo", boardSimulado.getTitulo()); // El título original quedó intacto
        verify(boardRepository, never()).save(any());
    }
}