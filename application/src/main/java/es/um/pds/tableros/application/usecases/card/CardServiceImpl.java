package es.um.pds.tableros.application.usecases.card;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import es.um.pds.tableros.application.usecases.events.CardMovidaEvent;
import es.um.pds.tableros.domain.board.Board;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.board.ListId;
import es.um.pds.tableros.domain.board.Rol;
import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.card.CardType;
import es.um.pds.tableros.domain.card.Etiqueta;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.card.commands.AnadirEtiquetaCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;
import es.um.pds.tableros.domain.ports.output.BoardRepository;
import es.um.pds.tableros.domain.ports.output.CardRepository;
import es.um.pds.tableros.domain.services.CardMovementService;

/**
 * @brief Implementación del Puerto de Entrada CardService. Orquesta los casos de uso
 * asociados al ciclo de vida de las tarjetas, comunicándose con los repositorios,
 * validando permisos, invocando al servicio de dominio (CardMovementService) y publicando eventos.
 */
@Service
public class CardServiceImpl implements CardService {

    private static final Logger log = LoggerFactory.getLogger(CardServiceImpl.class);

    private final CardRepository cardRepository;
    private final BoardRepository boardRepository;
    private final CardMovementService cardMovementService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * @brief Constructor para inyectar los repositorios, servicios de dominio y el publicador de eventos.
     * @param cardRepository Puerto de salida para persistencia de tarjetas.
     * @param boardRepository Puerto de salida para acceso a tableros (validación de reglas).
     * @param cardMovementService Servicio de dominio que orquesta el movimiento de tarjetas.
     * @param eventPublisher Componente de Spring para la emisión asíncrona de eventos.
     */
    public CardServiceImpl(CardRepository cardRepository, 
                           BoardRepository boardRepository, 
                           CardMovementService cardMovementService,
                           ApplicationEventPublisher eventPublisher) {
        this.cardRepository = cardRepository;
        this.boardRepository = boardRepository;
        this.cardMovementService = cardMovementService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * @brief Recupera una tarjeta transformando el identificador crudo al Value Object correspondiente.
     * @param id Cadena de texto con el ID de la tarjeta.
     * @return Un Optional que contiene la tarjeta si el repositorio la encuentra.
     */
    @Override
    public Optional<Card> obtenerTarjetaPorId(String id) {
        return this.cardRepository.findById(new CardId(id)); // Protegemos convirtiendo a CardId aquí
    }

    /**
     * @brief Recupera la colección completa de tarjetas asociadas a un tablero específico.
     * @param boardId Identificador del tablero en formato texto.
     * @return Lista con todas las tarjetas pertenecientes al tablero.
     */
    @Override
    public List<Card> obtenerTarjetasPorTablero(String boardId) {
        // El caso de uso protege al dominio transformando el String al VO
        BoardId idDominio = new BoardId(boardId);
        return this.cardRepository.findByBoardId(idDominio);
    }
    
    /**
     * @brief Instancia y persiste una nueva tarjeta, validando previamente los límites del tablero y los permisos.
     * @param cmd Estructura con la configuración inicial de la tarjeta (título, tipo, etiquetas, checklist).
     * @return La tarjeta generada con su nuevo estado y configuración.
     * @throws IllegalArgumentException Si el tablero referenciado no existe en base de datos.
     * @throws IllegalStateException Si el usuario no tiene permisos de escritura o si la lista está llena/bloqueada.
     */
    @Override
    public Card crearNuevaTarjeta(CrearCardCommand cmd) {
        log.info("Creando nueva tarjeta con título '{}' en la lista {}", cmd.titulo(), cmd.listId());

        BoardId bId = new BoardId(cmd.boardId());
        ListId lId = new ListId(cmd.listId());
        
        Board board = boardRepository.findById(bId)
             .orElseThrow(() -> new IllegalArgumentException("El tablero especificado no existe"));
        
        // --- NUEVO: Verificamos permisos ---
        verificarPermisoEscritura(board, cmd.emailSolicitante());

        // Validamos que el tablero admita nuevas tarjetas en la lista destino (límite WIP y estado de bloqueo)
        board.verificaAnadirCard(lId);

        CardId nuevoCardId = CardId.generate();
        CardType tipo = CardType.valueOf(cmd.tipo().toUpperCase());
        
        Card nuevaTarjeta = new Card(nuevoCardId, bId, lId, cmd.titulo(), tipo);
        
        // Inicializamos los elementos del checklist si existen
        if (cmd.checklistItems() != null && !cmd.checklistItems().isEmpty()) {
            for (String itemText : cmd.checklistItems()) {
                nuevaTarjeta.anadirChecklistItem(itemText);
            }
        }

        // Creamos el objeto de dominio Etiqueta DENTRO del servicio
        if (cmd.nombreEtiqueta() != null && cmd.colorEtiqueta() != null) {
            nuevaTarjeta.anadirEtiqueta(new Etiqueta(cmd.nombreEtiqueta(), cmd.colorEtiqueta()));
        }
        
        this.cardRepository.save(nuevaTarjeta);
        
        // Sincronizamos los contadores del tablero y añadimos el evento al historial
        board.registrarMovimientoTarjeta(null, lId);
        board.registrarEvento("Nueva tarjeta creada: '" + cmd.titulo() + "' en la lista " + board.obtenerNombreLista(lId));
        boardRepository.save(board);

        return nuevaTarjeta;
    }

    /**
     * @brief Orquesta el traslado de una tarjeta mediante el Servicio de Dominio y emite un evento al sistema.
     * @param cmd Comando con los identificadores de la tarjeta, el tablero contenedor y la lista de destino.
     * @throws IllegalArgumentException Si la tarjeta o el tablero no existen.
     * @throws IllegalStateException Si el usuario carece de permisos o se viola alguna regla del tablero (ej. WIP limit).
     */
    @Override
    public void moverTarjeta(MoverCardCommand cmd) {
        log.info("Iniciando movimiento de la tarjeta {} al destino {}", cmd.cardId(), cmd.targetListId());

        Card card = this.cardRepository.findById(new CardId(cmd.cardId()))
                .orElseThrow(() -> new IllegalArgumentException("La tarjeta no existe"));

        Board board = this.boardRepository.findById(new BoardId(cmd.boardId()))
                .orElseThrow(() -> new IllegalArgumentException("El tablero no existe"));

        // --- NUEVO: Verificamos permisos ---
        verificarPermisoEscritura(board, cmd.emailSolicitante());

        // Delegamos la lógica compleja de actualización múltiple al servicio de dominio
        String traceLog = this.cardMovementService.moveCard(card, board, new ListId(cmd.targetListId()));
        log.info(traceLog);

        board.registrarEvento(traceLog);
        
        this.cardRepository.save(card);
        this.boardRepository.save(board);
        
        // Disparamos el evento para que los listeners (como AutomationService) lo recojan
        eventPublisher.publishEvent(new CardMovidaEvent(cmd.boardId(), cmd.cardId(), cmd.targetListId()));
    }

    /**
     * @brief Asigna una etiqueta de color a una tarjeta existente.
     * @param cmd Comando con el identificador de la tarjeta y los datos de la etiqueta.
     * @throws IllegalArgumentException Si la tarjeta no es encontrada en el sistema.
     */
    @Override
    public void anadirEtiqueta(AnadirEtiquetaCommand cmd) {
        log.info("Añadiendo etiqueta '{}' a la tarjeta {}", cmd.nombre(), cmd.cardId());
        
        Card card = this.cardRepository.findById(new CardId(cmd.cardId()))
                .orElseThrow(() -> new IllegalArgumentException("La tarjeta no existe"));

        card.anadirEtiqueta(new Etiqueta(cmd.nombre(), cmd.color()));
        
        this.cardRepository.save(card);
    }
    
    /**
     * @brief Método auxiliar interno (Helper) para aplicar la validación de permisos al interactuar con tarjetas.
     * @param board El tablero al que pertenece la tarjeta.
     * @param emailSolicitante El correo de quien ejecuta la acción.
     * @throws IllegalStateException Si no tiene permisos de modificación o no está autenticado.
     */
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