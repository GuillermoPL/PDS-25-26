package es.um.pds.tableros.application.usecases.board;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.ports.input.board.CompactacionTablerosService;
import es.um.pds.tableros.domain.ports.output.BoardRepository;
import es.um.pds.tableros.domain.ports.output.CardRepository;

@Service
public class CompactacionTablerosServiceImpl implements CompactacionTablerosService {

	private static final Logger log = LoggerFactory.getLogger(CompactacionTablerosServiceImpl.class);
    private static final int DIAS_ANTIGUEDAD = 7; 

    private final BoardRepository boardRepository;
    private final CardRepository cardRepository;

    public CompactacionTablerosServiceImpl(BoardRepository boardRepository, CardRepository cardRepository) {
        this.boardRepository = boardRepository;
        this.cardRepository = cardRepository;
    }

    @Override
    public void ejecutarCompactacion() {
        log.info("Iniciando proceso automático de compactación de tableros...");
        int tarjetasBorradasTotal = 0;

        List<Board> todosLosTableros = boardRepository.findAll(); // Asegúrate de tener findAll() en tu BoardRepository, si no, añádelo igual que el delete

        for (Board board : todosLosTableros) {
            List<Card> tarjetasDelTablero = cardRepository.findByBoardId(board.getId());
            int tarjetasBorradasEnTablero = 0;

            for (Card card : tarjetasDelTablero) {
                // Si NO está completada y ES antigua
                if (!card.isCompletada() && card.esAntigua(DIAS_ANTIGUEDAD)) {
                    board.registrarMovimientoTarjeta(card.getListIdActual(), null);
                    cardRepository.delete(card.getId()); // Borramos la tarjeta
                    tarjetasBorradasEnTablero++;
                    tarjetasBorradasTotal++;
                }
            }

            if (tarjetasBorradasEnTablero > 0) {
                board.registrarEvento("Compactación automática: Se han archivado " + tarjetasBorradasEnTablero + " tareas antiguas sin completar.");
                boardRepository.save(board);
            }
        }

        log.info("Proceso de compactación finalizado. Total de tarjetas antiguas archivadas: {}", tarjetasBorradasTotal);
    }
}