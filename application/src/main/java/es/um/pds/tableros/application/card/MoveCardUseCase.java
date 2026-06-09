package es.um.pds.tableros.application.card;

import org.springframework.stereotype.Service;
import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.ports.output.BoardRepository;
import es.um.pds.tableros.domain.ports.output.CardRepository;
import es.um.pds.tableros.domain.services.CardMovementService;

@Service
public class MoveCardUseCase {

    private final CardRepository cardRepository;
    private final BoardRepository boardRepository;
    private final CardMovementService cardMovementService;

    // Inyección de dependencias por constructor
    public MoveCardUseCase(CardRepository cardRepository, BoardRepository boardRepository, CardMovementService cardMovementService) {
        this.cardRepository = cardRepository;
        this.boardRepository = boardRepository;
        this.cardMovementService = cardMovementService;
    }

    public void execute(String cardIdStr, String targetListIdStr) {
        // Convertimos los datos de entrada a Value Objects del dominio
        CardId cardId = new CardId(cardIdStr);
        ListId targetListId = new ListId(targetListIdStr);

        // Buscamos la Card en el repositorio
        Card card = cardRepository.findById(cardId)
                				  .orElseThrow(() -> new IllegalArgumentException("Tarjeta no encontrada"));

        // Buscamos el Board asociado en el repositorio
        Board board = boardRepository.findById(card.getBoardId())
        							 .orElseThrow(() -> new IllegalArgumentException("Tablero no encontrado"));

        // Llamamos al servicio de dominio
        cardMovementService.moveCard(card, board, targetListId);

        // Guardamos los cambios en la base de datos
        cardRepository.save(card);
        boardRepository.save(board);
    }
}