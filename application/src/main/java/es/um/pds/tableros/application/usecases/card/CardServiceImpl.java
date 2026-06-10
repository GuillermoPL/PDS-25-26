package es.um.pds.tableros.application.usecases.card;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.card.CardType;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;

// Puertos de salida
import es.um.pds.tableros.domain.ports.output.CardRepository;
import es.um.pds.tableros.domain.ports.output.BoardRepository; 

import es.um.pds.tableros.domain.services.CardMovementService;

@Service
public class CardServiceImpl implements CardService {

    private static final Logger log = LoggerFactory.getLogger(CardServiceImpl.class);

    private final CardRepository cardRepository;
    private final BoardRepository boardRepository;
    private final CardMovementService cardMovementService;

    // Inyección por constructor (A Spring le encanta esto)
    public CardServiceImpl(CardRepository cardRepository, 
                           BoardRepository boardRepository, 
                           CardMovementService cardMovementService) {
        this.cardRepository = cardRepository;
        this.boardRepository = boardRepository;
        this.cardMovementService = cardMovementService;
    }

    @Override
    public Optional<Card> obtenerTarjetaPorId(CardId id) {
        return this.cardRepository.findById(id);
    }

    @Override
    public List<Card> obtenerTarjetasPorTablero(BoardId boardId) {
        return this.cardRepository.findByBoardId(boardId);
    }

    @Override
    public Card crearNuevaTarjeta(CrearCardCommand cmd) {
        log.info("Creando nueva tarjeta con título '{}' en la lista {}", cmd.titulo(), cmd.listId());

        BoardId bId = new BoardId(cmd.boardId());
        ListId lId = new ListId(cmd.listId());
        
        //TODO Crear excepciones concretas para errores de tarjetas ( y de tableros)
        // 1. Recuperar el tablero para verificar si se puede añadir la tarjeta (invariantes)
        Board board = boardRepository.findById(bId)
             .orElseThrow(() -> new IllegalArgumentException("El tablero especificado no existe"));
        
        // 2. Ejecutar la validación del tablero (si está bloqueado o lista llena)
        board.verificaAnadirCard(lId);

        // 3. Si todo es correcto, instanciamos la Tarjeta con un ID nuevo generado
        CardId nuevoCardId = CardId.generate();
        CardType tipo = CardType.valueOf(cmd.tipo().toUpperCase());
        
        Card nuevaTarjeta = new Card(nuevoCardId, bId, lId, cmd.titulo(), tipo);

        // 4. Persistir a través del puerto de salida e incrementar contador del tablero
        this.cardRepository.save(nuevaTarjeta);
        board.registrarMovimientoTarjeta(null, lId); // Al ser nueva, origen es null
        
        board.registrarEvento("Nueva tarjeta creada: '" + cmd.titulo() + "' en la lista " + lId.value());
        
        
        boardRepository.save(board);

        return nuevaTarjeta;
    }

    @Override
    public void moverTarjeta(MoverCardCommand cmd) {
        log.info("Iniciando movimiento de la tarjeta {} al destino {}", cmd.cardId(), cmd.targetListId());

        // 1. Recuperar la tarjeta de la persistencia
        Card card = this.cardRepository.findById(new CardId(cmd.cardId()))
                .orElseThrow(() -> new IllegalArgumentException("La tarjeta no existe"));

        // 2. Recuperar el tablero
        Board board = this.boardRepository.findById(new BoardId(cmd.boardId()))
                .orElseThrow(() -> new IllegalArgumentException("El tablero no existe"));

        // 3. Delegar la lógica y las validaciones complejas al Servicio de Dominio que ya teníais
        String traceLog = this.cardMovementService.moveCard(card, board, new ListId(cmd.targetListId()));
        log.info(traceLog);

        board.registrarEvento(traceLog);
        
        // 4. Guardar los cambios de ambos agregados en la base de datos
        this.cardRepository.save(card);
        this.boardRepository.save(board);
    }
}