package es.um.pds.tableros.test.application.usecases.card;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher; // AÑADIDO: Import necesario

import es.um.pds.tableros.application.usecases.card.CardServiceImpl;
import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.card.CardType;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;
import es.um.pds.tableros.domain.ports.output.BoardRepository;
import es.um.pds.tableros.domain.ports.output.CardRepository;
import es.um.pds.tableros.domain.services.CardMovementService;

/**
 * @brief Pruebas unitarias para el Servicio de Aplicación de Tarjetas (CardServiceImpl).
 * Valida la correcta orquestación entre repositorios, el servicio de dominio (CardMovementService)
 * y el manejador de eventos de la aplicación (ApplicationEventPublisher).
 */
@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private CardMovementService cardMovementService;

    // AÑADIDO: Mockeamos el publicador de eventos para que no sea null
    @Mock
    private ApplicationEventPublisher eventPublisher; 

    @InjectMocks
    private CardServiceImpl cardService;

    /**
     * @brief Verifica la creación de una tarea simple (tipo TASK) sin etiquetas iniciales.
     * Asegura que se instancian sus datos básicos, que se delega la regla al tablero
     * y que se guardan los estados de ambos agregados.
     */
    @Test
    void testCrearNuevaTarjetaSinEtiqueta() {
        Board boardSimulado = new Board(new BoardId("b1"), "Tablero", new Email("test@um.es"));
        boardSimulado.addList("To Do", 10);
        String validListId = boardSimulado.getTasksLists().get(0).getId().value();

        when(boardRepository.findById(new BoardId("b1"))).thenReturn(Optional.of(boardSimulado));

        CrearCardCommand cmd = new CrearCardCommand("b1", validListId, "Nueva Tarea", "TASK", null, null, null, "test@um.es");
        
        Card nuevaTarjeta = cardService.crearNuevaTarjeta(cmd);

        assertEquals("Nueva Tarea", nuevaTarjeta.getTitulo());
        assertEquals(CardType.TASK, nuevaTarjeta.getTipo());
        assertEquals(validListId, nuevaTarjeta.getListIdActual().value());
        assertTrue(nuevaTarjeta.getEtiquetas().isEmpty());

        verify(cardRepository, times(1)).save(any(Card.class));
        verify(boardRepository, times(1)).save(boardSimulado);
    }

    /**
     * @brief Valida la creación de una tarea inyectando una etiqueta directamente desde el comando.
     * Comprueba que la entidad de dominio Etiqueta se construye correctamente en el servicio.
     */
    @Test
    void testCrearNuevaTarjetaConEtiqueta() {
        Board boardSimulado = new Board(new BoardId("b1"), "Tablero", new Email("test@um.es"));
        boardSimulado.addList("To Do", 10);
        String validListId = boardSimulado.getTasksLists().get(0).getId().value();

        when(boardRepository.findById(new BoardId("b1"))).thenReturn(Optional.of(boardSimulado));

        CrearCardCommand cmd = new CrearCardCommand("b1", validListId, "Nueva Tarea", "TASK", "Urgente", "#ff0000", null, "test@um.es");

        Card nuevaTarjeta = cardService.crearNuevaTarjeta(cmd);

        assertEquals("Nueva Tarea", nuevaTarjeta.getTitulo());
        assertEquals(1, nuevaTarjeta.getEtiquetas().size());
        assertEquals("Urgente", nuevaTarjeta.getEtiquetas().get(0).nombre());
        assertEquals("#ff0000", nuevaTarjeta.getEtiquetas().get(0).color());

        verify(cardRepository, times(1)).save(any(Card.class));
        verify(boardRepository, times(1)).save(boardSimulado);
    }

    /**
     * @brief Verifica la inicialización de tarjetas complejas (tipo CHECKLIST) con elementos precargados.
     */
    @Test
    void testCrearNuevaTarjetaChecklist() {
        Board boardSimulado = new Board(new BoardId("b1"), "Tablero", new Email("test@um.es"));
        boardSimulado.addList("To Do", 10);
        String validListId = boardSimulado.getTasksLists().get(0).getId().value();

        when(boardRepository.findById(new BoardId("b1"))).thenReturn(Optional.of(boardSimulado));

        List<String> pasos = List.of("Paso 1", "Paso 2");
        CrearCardCommand cmd = new CrearCardCommand("b1", validListId, "Revisión", "CHECKLIST", null, null, pasos, "test@um.es");
        
        Card nuevaTarjeta = cardService.crearNuevaTarjeta(cmd);

        assertEquals("Revisión", nuevaTarjeta.getTitulo());
        assertEquals(CardType.CHECKLIST, nuevaTarjeta.getTipo());
        assertEquals(2, nuevaTarjeta.getChecklistItems().size());
        assertEquals("Paso 1", nuevaTarjeta.getChecklistItems().get(0));

        verify(cardRepository, times(1)).save(any(Card.class));
        verify(boardRepository, times(1)).save(boardSimulado);
    }

    /**
     * @brief Comprueba la orquestación del movimiento de una tarjeta.
     * Valida que el servicio delega la responsabilidad algorítmica al CardMovementService,
     * sincroniza la base de datos para los agregados afectados y, finalmente, publica
     * el evento asíncrono en el bus de la aplicación.
     */
    @Test
    void testMoverTarjetaDelegaAlServicioDeDominioYGuarda() {
        Card cardSimulada = new Card(new CardId("c1"), new BoardId("b1"), new ListId("l1"), "Tarea", CardType.TASK);
        Board boardSimulado = new Board(new BoardId("b1"), "Tablero", new Email("t@um.es"));

        when(cardRepository.findById(new CardId("c1"))).thenReturn(Optional.of(cardSimulada));
        when(boardRepository.findById(new BoardId("b1"))).thenReturn(Optional.of(boardSimulado));
        when(cardMovementService.moveCard(eq(cardSimulada), eq(boardSimulado), any(ListId.class)))
            .thenReturn("Traza de prueba");

        MoverCardCommand cmd = new MoverCardCommand("c1", "b1", "l2", "t@um.es");
        
        cardService.moverTarjeta(cmd);

        verify(cardMovementService, times(1)).moveCard(eq(cardSimulada), eq(boardSimulado), eq(new ListId("l2")));
        verify(cardRepository, times(1)).save(cardSimulada);
        verify(boardRepository, times(1)).save(boardSimulado);
        
        // OPCIONAL: Podemos incluso verificar que se lanzó el evento correctamente
        verify(eventPublisher, times(1)).publishEvent(any(Object.class));
    }
}