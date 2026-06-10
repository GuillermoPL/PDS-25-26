package es.um.pds.tableros.test.application.usecases.card;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private CardMovementService cardMovementService;

    @InjectMocks
    private CardServiceImpl cardService;

    @Test
    void testCrearNuevaTarjeta() {
        // 1. Creamos un tablero real válido para que pase las reglas de negocio
        Board boardSimulado = new Board(new BoardId("b1"), "Tablero", new Email("test@um.es"));
        boardSimulado.addList("To Do", 10);
        
        // Obtenemos el ID real generado de la lista para usarlo en el comando
        String validListId = boardSimulado.getTasksLists().get(0).getId().value();
        when(boardRepository.findById(new BoardId("b1"))).thenReturn(Optional.of(boardSimulado));

        CrearCardCommand cmd = new CrearCardCommand("b1", validListId, "Nueva Tarea", "TASK", "Urgente");
        // 2. Ejecutamos
        Card nuevaTarjeta = cardService.crearNuevaTarjeta(cmd);

        // 3. Comprobamos el estado del objeto creado
        assertEquals("Nueva Tarea", nuevaTarjeta.getTitulo());
        assertEquals(CardType.TASK, nuevaTarjeta.getTipo());
        assertEquals(validListId, nuevaTarjeta.getListIdActual().value());

        // Comprobamos que el servicio guardó tanto la tarjeta como el tablero (porque se actualizó su contador)
        verify(cardRepository, times(1)).save(any(Card.class));
        verify(boardRepository, times(1)).save(boardSimulado);
    }

    @Test
    void testMoverTarjetaDelegaAlServicioDeDominioYGuarda() {
        // 1. Configuramos los mocks de búsqueda
        Card cardSimulada = new Card(new CardId("c1"), new BoardId("b1"), new ListId("l1"), "Tarea", CardType.TASK);
        Board boardSimulado = new Board(new BoardId("b1"), "Tablero", new Email("t@um.es"));
        
        when(cardRepository.findById(new CardId("c1"))).thenReturn(Optional.of(cardSimulada));
        when(boardRepository.findById(new BoardId("b1"))).thenReturn(Optional.of(boardSimulado));
        
        // Simulamos que el Servicio de Dominio nos devuelve un log de traza sin hacer nada más
        when(cardMovementService.moveCard(eq(cardSimulada), eq(boardSimulado), any(ListId.class)))
            .thenReturn("Traza de prueba");

        MoverCardCommand cmd = new MoverCardCommand("c1", "b1", "l2");

        // 2. Ejecutamos
        cardService.moverTarjeta(cmd);

        // 3. Comprobamos que el servicio de aplicación coordinó todo
        // Nos aseguramos de que llamó al servicio de dominio
        verify(cardMovementService, times(1)).moveCard(eq(cardSimulada), eq(boardSimulado), eq(new ListId("l2")));
        
        // Nos aseguramos de que guardó los cambios de ambos agregados
        verify(cardRepository, times(1)).save(cardSimulada);
        verify(boardRepository, times(1)).save(boardSimulado);
    }
}