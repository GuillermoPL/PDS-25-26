package es.um.pds.tableros.application.usecases.card;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;

import es.um.pds.tableros.application.usecases.events.CardMovidaEvent;
import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.board.Rol;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.card.CardType;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.AnadirEtiquetaCommand;
import es.um.pds.tableros.domain.ports.output.CardRepository;
import es.um.pds.tableros.domain.ports.output.BoardRepository; 
import es.um.pds.tableros.domain.services.CardMovementService;
import es.um.pds.tableros.domain.card.Etiqueta;

@Service
public class CardServiceImpl implements CardService {

    private static final Logger log = LoggerFactory.getLogger(CardServiceImpl.class);

    private final CardRepository cardRepository;
    private final BoardRepository boardRepository;
    private final CardMovementService cardMovementService;
    private final ApplicationEventPublisher eventPublisher;

    public CardServiceImpl(CardRepository cardRepository, 
                           BoardRepository boardRepository, 
                           CardMovementService cardMovementService,
                           ApplicationEventPublisher eventPublisher) {
        this.cardRepository = cardRepository;
        this.boardRepository = boardRepository;
        this.cardMovementService = cardMovementService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Card> obtenerTarjetaPorId(String id) {
        return this.cardRepository.findById(new CardId(id)); // Protegemos convirtiendo a CardId aquí
    }

    @Override
    public List<Card> obtenerTarjetasPorTablero(String boardId) {
        // El caso de uso protege al dominio transformando el String al VO
        BoardId idDominio = new BoardId(boardId);
        return this.cardRepository.findByBoardId(idDominio);
    }
    @Override
    public Card crearNuevaTarjeta(CrearCardCommand cmd) {
        log.info("Creando nueva tarjeta con título '{}' en la lista {}", cmd.titulo(), cmd.listId());

        BoardId bId = new BoardId(cmd.boardId());
        ListId lId = new ListId(cmd.listId());
        
        Board board = boardRepository.findById(bId)
             .orElseThrow(() -> new IllegalArgumentException("El tablero especificado no existe"));
        
        // --- NUEVO: Verificamos permisos ---
        verificarPermisoEscritura(board, cmd.emailSolicitante());

        board.verificaAnadirCard(lId);

        CardId nuevoCardId = CardId.generate();
        CardType tipo = CardType.valueOf(cmd.tipo().toUpperCase());
        
        Card nuevaTarjeta = new Card(nuevoCardId, bId, lId, cmd.titulo(), tipo);
        if (cmd.checklistItems() != null && !cmd.checklistItems().isEmpty()) {
            for (String itemText : cmd.checklistItems()) {
                nuevaTarjeta.anadirChecklistItem(itemText);
            }
        }

        // --- NUEVO: Creamos el objeto de dominio Etiqueta DENTRO del servicio ---
        if (cmd.nombreEtiqueta() != null && cmd.colorEtiqueta() != null) {
            nuevaTarjeta.anadirEtiqueta(new Etiqueta(cmd.nombreEtiqueta(), cmd.colorEtiqueta()));
        }
        
        this.cardRepository.save(nuevaTarjeta);
        board.registrarMovimientoTarjeta(null, lId);
        
        board.registrarEvento("Nueva tarjeta creada: '" + cmd.titulo() + "' en la lista " + board.obtenerNombreLista(lId));
        boardRepository.save(board);

        return nuevaTarjeta;
    }

    @Override
    public void moverTarjeta(MoverCardCommand cmd) {
        log.info("Iniciando movimiento de la tarjeta {} al destino {}", cmd.cardId(), cmd.targetListId());

        Card card = this.cardRepository.findById(new CardId(cmd.cardId()))
                .orElseThrow(() -> new IllegalArgumentException("La tarjeta no existe"));

        Board board = this.boardRepository.findById(new BoardId(cmd.boardId()))
                .orElseThrow(() -> new IllegalArgumentException("El tablero no existe"));

        // --- NUEVO: Verificamos permisos ---
        verificarPermisoEscritura(board, cmd.emailSolicitante());

        String traceLog = this.cardMovementService.moveCard(card, board, new ListId(cmd.targetListId()));
        log.info(traceLog);

        board.registrarEvento(traceLog);
        
        this.cardRepository.save(card);
        this.boardRepository.save(board);
        
        eventPublisher.publishEvent(new CardMovidaEvent(cmd.boardId(), cmd.cardId(), cmd.targetListId()));
    }

    @Override
    public void anadirEtiqueta(AnadirEtiquetaCommand cmd) {
        log.info("Añadiendo etiqueta '{}' a la tarjeta {}", cmd.nombre(), cmd.cardId());
        
        Card card = this.cardRepository.findById(new CardId(cmd.cardId()))
                .orElseThrow(() -> new IllegalArgumentException("La tarjeta no existe"));

        card.anadirEtiqueta(new Etiqueta(cmd.nombre(), cmd.color()));
        
        this.cardRepository.save(card);
    }
    
 // --- HELPER PRIVADO EN LA CAPA DE APLICACIÓN ---
    private void verificarPermisoEscritura(Board board, String emailSolicitante) {
        if (emailSolicitante == null || emailSolicitante.isBlank()) {
            throw new IllegalStateException("Usuario no autenticado");
        }
        Rol rol = board.obtenerRol(new Email(emailSolicitante));
        if (!Rol.WRITE.equals(rol)) {
            throw new IllegalStateException("El usuario no tiene permisos de escritura en este tablero");
        }
    }
}