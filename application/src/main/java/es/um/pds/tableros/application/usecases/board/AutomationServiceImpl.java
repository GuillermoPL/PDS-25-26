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
import es.um.pds.tableros.domain.ports.output.BoardRepository;

@Service
public class AutomationServiceImpl {

    private final BoardRepository boardRepository;
    private final CardService cardService;

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
                ejecutarAccion(regla.actionType(), board, evento.cardId());
            }
        }
    }

    private void ejecutarAccion(ActionType accion, Board board, String cardId) {
        switch (accion) {
            case MARCAR_COMO_COMPLETADA -> {
                // Si el tablero tiene definida una lista de completadas, movemos la tarjeta allí
                if (board.getListCompletadas() != null) {
                    MoverCardCommand cmd = new MoverCardCommand(
                        cardId, 
                        board.getId().value(), 
                        board.getListCompletadas().value(),
                        board.getEmail().value() // AÑADIDO: Pasamos el email del dueño para pasar la seguridad del sistema automático
                    );
                    cardService.moverTarjeta(cmd);
                }
            }
            case AÑADIR_ETIQUETA_ROJA -> {
                AnadirEtiquetaCommand cmd = new AnadirEtiquetaCommand(cardId, "Automatizado", "#e74c3c");
                cardService.anadirEtiqueta(cmd);
            }
        }
    }
}