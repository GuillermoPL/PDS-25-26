package es.um.pds.tableros.test.application.usecases.board;

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

import es.um.pds.tableros.application.usecases.board.AutomationServiceImpl;
import es.um.pds.tableros.application.usecases.events.CardMovidaEvent;
import es.um.pds.tableros.domain.board.ActionType;
import es.um.pds.tableros.domain.board.AutomationRule;
import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.board.TriggerType;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.AnadirEtiquetaCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;
import es.um.pds.tableros.domain.ports.output.BoardRepository;

/**
 * @brief Pruebas unitarias e integración para el Servicio de Automatización (AutomationServiceImpl).
 * Valida que los eventos de dominio desencadenan correctamente las reglas configuradas
 * en los tableros, utilizando Mockito para interceptar las llamadas al CardService.
 */
@ExtendWith(MockitoExtension.class)
class AutomationServiceImplTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private AutomationServiceImpl automationService;

    /**
     * @brief Verifica que si el evento no coincide con el trigger de la regla, no se ejecuta ninguna acción.
     */
    @Test
    void testProcesarReglasIgnoraSiTriggerNoCoincide() {
        // 1. Preparar un tablero con una regla para la lista "l_destino_real"
        Board boardSimulado = new Board(new BoardId("b1"), "Tablero Auto", new Email("test@um.es"));
        AutomationRule regla = new AutomationRule("r1", TriggerType.TARJETA_MOVIDA_A_LISTA, "l_destino_real", ActionType.AÑADIR_ETIQUETA_ROJA);
        boardSimulado.anadirRegla(regla);
        
        when(boardRepository.findById(new BoardId("b1"))).thenReturn(Optional.of(boardSimulado));

        // Evento que mueve la tarjeta a una lista diferente ("l_otra")
        CardMovidaEvent evento = new CardMovidaEvent("b1", "c1", "l_otra");

        // 2. Ejecutar
        automationService.procesarReglasPorMovimiento(evento);

        // 3. Verificar que NO se ha llamado al servicio de tarjetas
        verify(cardService, never()).anadirEtiqueta(any(AnadirEtiquetaCommand.class));
        verify(cardService, never()).moverTarjeta(any(MoverCardCommand.class));
    }

    /**
     * @brief Verifica que la regla de añadir etiqueta se ejecuta correctamente cuando el trigger coincide.
     */
    @Test
    void testProcesarReglasEjecutaAccionAnadirEtiqueta() {
        // 1. Preparar
        Board boardSimulado = new Board(new BoardId("b1"), "Tablero Auto", new Email("test@um.es"));
        AutomationRule regla = new AutomationRule("r1", TriggerType.TARJETA_MOVIDA_A_LISTA, "l_peligro", ActionType.AÑADIR_ETIQUETA_ROJA);
        boardSimulado.anadirRegla(regla);
        
        when(boardRepository.findById(new BoardId("b1"))).thenReturn(Optional.of(boardSimulado));

        // Evento que coincide con la lista de la regla
        CardMovidaEvent evento = new CardMovidaEvent("b1", "c1", "l_peligro");

        // 2. Ejecutar
        automationService.procesarReglasPorMovimiento(evento);

        // 3. Verificar que se envía el comando correcto para añadir la etiqueta
        verify(cardService, times(1)).anadirEtiqueta(any(AnadirEtiquetaCommand.class));
    }

    /**
     * @brief Verifica que la regla de marcar como completada traslada la tarjeta a la lista configurada.
     */
    @Test
    void testProcesarReglasEjecutaAccionMarcarCompletada() {
        // 1. Preparar
        Board boardSimulado = new Board(new BoardId("b1"), "Tablero Auto", new Email("test@um.es"));
        // El tablero DEBE tener una lista de completadas configurada para que esta regla funcione
        boardSimulado.addList("Hecho", 10);
        String listCompletadasId = boardSimulado.getTasksLists().get(0).getId().value();
        boardSimulado.defineListCompletadas(new ListId(listCompletadasId));

        AutomationRule regla = new AutomationRule("r1", TriggerType.TARJETA_MOVIDA_A_LISTA, "l_revision", ActionType.MARCAR_COMO_COMPLETADA);
        boardSimulado.anadirRegla(regla);
        
        when(boardRepository.findById(new BoardId("b1"))).thenReturn(Optional.of(boardSimulado));

        // Evento
        CardMovidaEvent evento = new CardMovidaEvent("b1", "c1", "l_revision");

        // 2. Ejecutar
        automationService.procesarReglasPorMovimiento(evento);

        // 3. Verificar que se envía el comando de mover a la lista de completadas
        verify(cardService, times(1)).moverTarjeta(any(MoverCardCommand.class));
    }
}