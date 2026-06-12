package es.um.pds.tableros.application.usecases.board;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import es.um.pds.tableros.application.usecases.events.CardMovidaEvent;
import es.um.pds.tableros.domain.board.ActionType;
import es.um.pds.tableros.domain.board.AutomationRule;
import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.TriggerType;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.AnadirEtiquetaCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;
// Necesitarás importar este comando cuando lo crees
// import es.um.pds.tableros.domain.ports.input.card.commands.AnadirEtiquetaCommand;
import es.um.pds.tableros.domain.ports.output.BoardRepository;

@Service
public class AutomationServiceImpl {

    private final BoardRepository boardRepository;
    private final CardService cardService;

    // 1. AÑADIDO: El constructor para que Spring inyecte las dependencias
    public AutomationServiceImpl(BoardRepository boardRepository, CardService cardService) {
        this.boardRepository = boardRepository;
        this.cardService = cardService;
    }

    @EventListener
    public void procesarReglasPorMovimiento(CardMovidaEvent evento) {
        Board board = boardRepository.findById(new BoardId(evento.boardId())).orElse(null);
        if (board == null || board.getReglas() == null) return;

        for (AutomationRule regla : board.getReglas()) {
            boolean saltaTrigger = regla.triggerType() == TriggerType.TARJETA_MOVIDA_A_LISTA 
                                && regla.triggerPayload().equals(evento.listaDestinoId());

            if (saltaTrigger) {
                // Le pasamos también el 'board' para poder leer su lista de completadas
                ejecutarAccion(regla.actionType(), board, evento.cardId());
            }
        }
    }

    // 2. AÑADIDO: La lógica real de las acciones (y recibimos el Board como parámetro)
    private void ejecutarAccion(ActionType accion, Board board, String cardId) {
        switch (accion) {
        case MARCAR_COMO_COMPLETADA -> {
            // Si el tablero tiene definida una lista de completadas, movemos la tarjeta allí
            if (board.getListCompletadas() != null) {
                MoverCardCommand cmd = new MoverCardCommand(
                    cardId, 
                    board.getId().value(), 
                    board.getListCompletadas().value() // <-- .value() aquí para pasarlo a String
                );
                cardService.moverTarjeta(cmd);
            }
        }
            case AÑADIR_ETIQUETA_ROJA -> {
                // Llama al servicio de tarjetas para que le ponga la etiqueta roja
                AnadirEtiquetaCommand cmd = new AnadirEtiquetaCommand(cardId, "Automatizado", "#e74c3c");
                cardService.anadirEtiqueta(cmd);
            }
        }
    }
}