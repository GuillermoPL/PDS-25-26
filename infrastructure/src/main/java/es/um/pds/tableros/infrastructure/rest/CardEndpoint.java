package es.um.pds.tableros.infrastructure.rest;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import es.um.pds.tableros.domain.card.Card;
import es.um.pds.tableros.domain.card.CardId;
import es.um.pds.tableros.domain.board.BoardId;
import es.um.pds.tableros.domain.board.Rol;
import es.um.pds.tableros.domain.board.Email;
import es.um.pds.tableros.domain.ports.input.card.CardService;
import es.um.pds.tableros.domain.ports.input.board.BoardService;
import es.um.pds.tableros.domain.ports.input.card.commands.CrearCardCommand;
import es.um.pds.tableros.domain.ports.input.card.commands.MoverCardCommand;
import es.um.pds.tableros.infrastructure.rest.dto.CardDTO;
import es.um.pds.tableros.infrastructure.mappers.CardMapper;

@RestController
@RequestMapping("/api/v1/tarjetas")
public class CardEndpoint {

    private static final Logger log = LoggerFactory.getLogger(CardEndpoint.class);

    private final CardService cardService;
    private final BoardService boardService; // NUEVO
    private final CardMapper cardMapper;

    public CardEndpoint(CardService cardService, BoardService boardService, CardMapper cardMapper) {
        this.cardService = cardService;
        this.boardService = boardService;
        this.cardMapper = cardMapper;
    }

    // 1. Obtener tarjeta por ID (READ es suficiente)
    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getTarjeta(@PathVariable String id) {
        log.info("Buscando tarjeta con ID: {}", id);
        Optional<Card> card = cardService.obtenerTarjetaPorId(new CardId(id));
        return card.map(c -> ResponseEntity.ok(cardMapper.toDTO(c)))
                   .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 2. Crear una nueva tarjeta
    @PostMapping
    public ResponseEntity<CardDTO> createTarjeta(
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario, 
            @RequestBody CardDTO dto) {
            
        log.info("Petición para crear tarjeta '{}' en la lista {}", dto.getTitulo(), dto.getListIdActual());
        
        if (dto.getId() != null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        // Validar permisos
        if (!tienePermisoEscritura(dto.getBoardId(), emailUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            es.um.pds.tableros.domain.card.Etiqueta etiquetaDominio = null;
            if (dto.getEtiquetas() != null && !dto.getEtiquetas().isEmpty()) {
                CardDTO.EtiquetaDTO etDto = dto.getEtiquetas().get(0);
                etiquetaDominio = new es.um.pds.tableros.domain.card.Etiqueta(etDto.getNombre(), etDto.getColor());
            }

            CrearCardCommand cmd = new CrearCardCommand(
                dto.getBoardId(), dto.getListIdActual(), dto.getTitulo(), 
                dto.getTipo(), etiquetaDominio, dto.getChecklistItems()
            );
            
            Card nuevaTarjeta = cardService.crearNuevaTarjeta(cmd);
            return ResponseEntity.status(HttpStatus.CREATED).body(cardMapper.toDTO(nuevaTarjeta));
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Error al crear la tarjeta: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // 3. Mover una tarjeta de lista
    @PutMapping("/{id}/movimiento")
    public ResponseEntity<Void> moverTarjeta(
            @PathVariable String id, 
            @RequestHeader(value = "X-User-Email", required = false) String emailUsuario,
            @RequestBody MoverTarjetaPayload payload) {
            
        log.info("Petición para mover la tarjeta {} a la lista {}", id, payload.targetListId());
        
        // Validar permisos
        if (!tienePermisoEscritura(payload.boardId(), emailUsuario)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            MoverCardCommand cmd = new MoverCardCommand(id, payload.boardId(), payload.targetListId());
            cardService.moverTarjeta(cmd);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.error("Movimiento denegado: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    // --- HELPER PRIVADO ---
    private boolean tienePermisoEscritura(String boardId, String emailUsuario) {
        if (emailUsuario == null) return false;
        return boardService.obtenerTableroPorId(new BoardId(boardId))
            .map(board -> Rol.WRITE.equals(board.obtenerRol(new Email(emailUsuario))))
            .orElse(false);
    }

    public record MoverTarjetaPayload(String boardId, String targetListId) {}
}